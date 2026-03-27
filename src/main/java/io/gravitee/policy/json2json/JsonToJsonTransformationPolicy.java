/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.json2json;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import io.gravitee.common.http.MediaType;
import io.gravitee.el.TemplateEngine;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.HttpHeaderNames;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.reactive.api.ExecutionFailure;
import io.gravitee.gateway.reactive.api.context.http.HttpMessageExecutionContext;
import io.gravitee.gateway.reactive.api.context.http.HttpPlainExecutionContext;
import io.gravitee.gateway.reactive.api.context.kafka.KafkaMessageExecutionContext;
import io.gravitee.gateway.reactive.api.message.Message;
import io.gravitee.gateway.reactive.api.message.kafka.KafkaMessage;
import io.gravitee.gateway.reactive.api.policy.http.HttpPolicy;
import io.gravitee.gateway.reactive.api.policy.kafka.KafkaPolicy;
import io.gravitee.policy.json2json.configuration.JsonToJsonTransformationPolicyConfiguration;
import io.gravitee.policy.v3.json2json.JsonToJsonTransformationPolicyV3;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import java.util.Optional;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class JsonToJsonTransformationPolicy extends JsonToJsonTransformationPolicyV3 implements HttpPolicy, KafkaPolicy {

    private static final String INVALID_JSON_TRANSFORMATION = "JSON_INVALID_SPECIFICATION";

    public JsonToJsonTransformationPolicy(final JsonToJsonTransformationPolicyConfiguration configuration) {
        super(configuration);
    }

    @Override
    public String id() {
        return "json-to-json";
    }

    @Override
    public Completable onRequest(HttpPlainExecutionContext ctx) {
        return ctx.request().onBody(body -> transformBody(ctx, body, ctx.request().headers()));
    }

    @Override
    public Completable onResponse(HttpPlainExecutionContext ctx) {
        return ctx.response().onBody(body -> transformBody(ctx, body, ctx.response().headers()));
    }

    @Override
    public Completable onMessageRequest(HttpMessageExecutionContext ctx) {
        return ctx.request().onMessage(message -> transformMessage(ctx, message));
    }

    @Override
    public Completable onMessageResponse(HttpMessageExecutionContext ctx) {
        return ctx.response().onMessage(message -> transformMessage(ctx, message));
    }

    @Override
    public Completable onMessageRequest(KafkaMessageExecutionContext ctx) {
        return ctx.request().onMessage(message -> transformMessage(ctx, message));
    }

    @Override
    public Completable onMessageResponse(KafkaMessageExecutionContext ctx) {
        return ctx.response().onMessage(message -> transformMessage(ctx, message));
    }

    private Maybe<Buffer> transformBody(final HttpPlainExecutionContext ctx, final Maybe<Buffer> body, HttpHeaders httpHeaders) {
        var jsonContentType = Optional.ofNullable(httpHeaders.get(HttpHeaderNames.CONTENT_TYPE)).map(v -> v.toLowerCase().contains("json"));
        if (jsonContentType.orElse(false)) {
            return applyJoltTransform(ctx.getTemplateEngine(), body, new HttpHeaderOperations(httpHeaders)).onErrorResumeWith(
                ctx.interruptBodyWith(
                    new ExecutionFailure(500).key(INVALID_JSON_TRANSFORMATION).message("Unable to apply JOLT transformation to payload")
                )
            );
        }

        return body;
    }

    private Maybe<Message> transformMessage(final HttpMessageExecutionContext ctx, final Message message) {
        HttpHeaders headers = message.headers();
        return applyJoltTransform(
            ctx.getTemplateEngine(message),
            Maybe.fromCallable(message::content),
            new HeaderOperations() {
                @Override
                public boolean isJsonContentType() {
                    return Optional.ofNullable(headers.get(HttpHeaderNames.CONTENT_TYPE))
                        .map(v -> v.toLowerCase().contains("json"))
                        .orElse(false);
                }

                @Override
                public void setContentLength(int length) {
                    headers.set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(length));
                }

                @Override
                public void setContentType(String type) {
                    headers.set(HttpHeaderNames.CONTENT_TYPE, type);
                }
            }
        )
            .map(message::content)
            .defaultIfEmpty(message)
            .toMaybe()
            .onErrorResumeWith(
                ctx.interruptMessageWith(
                    new ExecutionFailure(500).key(INVALID_JSON_TRANSFORMATION).message("Unable to apply JOLT transformation to payload")
                )
            );
    }

    private Maybe<KafkaMessage> transformMessage(final KafkaMessageExecutionContext ctx, final KafkaMessage message) {
        return applyJoltTransform(ctx.getTemplateEngine(message), Maybe.fromCallable(message::content), new KafkaHeaderOperations(message))
            .map(buffer -> (KafkaMessage) message.content(buffer))
            .defaultIfEmpty(message)
            .toMaybe()
            .onErrorResumeWith(
                Maybe.fromCompletable(ctx.executionContext().interruptWith(org.apache.kafka.common.protocol.Errors.UNKNOWN_SERVER_ERROR))
            );
    }

    private Maybe<Buffer> applyJoltTransform(
        final TemplateEngine templateEngine,
        final Maybe<Buffer> bufferUpstream,
        final HeaderOperations headerOps
    ) {
        var nonEmptyBuffer = bufferUpstream.filter(b -> b.length() > 0);
        var joltSpec = templateEngine
            .eval(configuration.getSpecification(), String.class)
            .map(specification -> Chainr.fromSpec(JsonUtils.jsonToList(specification)));

        return Maybe.zip(joltSpec, nonEmptyBuffer, (chainr, buffer) -> {
            Object inputJSON = JsonUtils.jsonToObject(buffer.toString());
            Object transformedOutput = chainr.transform(inputJSON);
            return Buffer.buffer(JsonUtils.toJsonString(transformedOutput));
        })
            .doOnSuccess(buffer -> {
                headerOps.setContentLength(buffer.length());
                if (configuration.isOverrideContentType()) {
                    headerOps.setContentType(MediaType.APPLICATION_JSON);
                }
            })
            .onErrorResumeNext(Maybe::error);
    }
}
