/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.HttpHeaderNames;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.jupiter.api.ExecutionFailure;
import io.gravitee.gateway.jupiter.api.context.GenericExecutionContext;
import io.gravitee.gateway.jupiter.api.context.HttpExecutionContext;
import io.gravitee.gateway.jupiter.api.context.MessageExecutionContext;
import io.gravitee.gateway.jupiter.api.message.Message;
import io.gravitee.gateway.jupiter.api.policy.Policy;
import io.gravitee.policy.json2json.configuration.JsonToJsonTransformationPolicyConfiguration;
import io.gravitee.policy.v3.json2json.JsonToJsonTransformationPolicyV3;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class JsonToJsonTransformationPolicy extends JsonToJsonTransformationPolicyV3 implements Policy {

    private static final String INVALID_JSON_TRANSFORMATION = "JSON_INVALID_SPECIFICATION";

    public JsonToJsonTransformationPolicy(final JsonToJsonTransformationPolicyConfiguration configuration) {
        super(configuration);
    }

    @Override
    public String id() {
        return "json-to-json";
    }

    @Override
    public Completable onRequest(HttpExecutionContext ctx) {
        return ctx.request().onBody(body -> transformBody(ctx, body, ctx.request().headers()));
    }

    @Override
    public Completable onResponse(HttpExecutionContext ctx) {
        return ctx.response().onBody(body -> transformBody(ctx, body, ctx.response().headers()));
    }

    @Override
    public Completable onMessageRequest(MessageExecutionContext ctx) {
        return ctx.request().onMessage(message -> transformMessage(ctx, message));
    }

    @Override
    public Completable onMessageResponse(MessageExecutionContext ctx) {
        return ctx.response().onMessage(message -> transformMessage(ctx, message));
    }

    private Maybe<Buffer> transformBody(final HttpExecutionContext ctx, final Maybe<Buffer> body, HttpHeaders httpHeaders) {
        return applyJoltTransform(ctx, body, httpHeaders)
            .onErrorResumeWith(
                ctx.interruptBodyWith(
                    new ExecutionFailure(500).key(INVALID_JSON_TRANSFORMATION).message("Unable to apply JOLT transformation to payload")
                )
            );
    }

    private Maybe<Message> transformMessage(final MessageExecutionContext ctx, final Message message) {
        return applyJoltTransform(ctx, Maybe.fromCallable(message::content), message.headers())
            .map(message::content)
            .defaultIfEmpty(message)
            .toMaybe()
            .onErrorResumeWith(
                ctx.interruptMessageWith(
                    new ExecutionFailure(500).key(INVALID_JSON_TRANSFORMATION).message("Unable to apply JOLT transformation to payload")
                )
            );
    }

    private Maybe<Buffer> applyJoltTransform(
        final GenericExecutionContext ctx,
        final Maybe<Buffer> bufferUpstream,
        final HttpHeaders httpHeaders
    ) {
        var nonEmptyBuffer = bufferUpstream.filter(b -> b.length() > 0);
        var joltSpec = ctx
            .getTemplateEngine()
            .eval(configuration.getSpecification(), String.class)
            .map(specification -> Chainr.fromSpec(JsonUtils.jsonToList(specification)));

        return Maybe
            .zip(
                joltSpec,
                nonEmptyBuffer,
                (
                    (chainr, buffer) -> {
                        Object inputJSON = JsonUtils.jsonToObject(buffer.toString());
                        Object transformedOutput = chainr.transform(inputJSON);
                        return Buffer.buffer(JsonUtils.toJsonString(transformedOutput));
                    }
                )
            )
            .doOnSuccess(buffer -> {
                httpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(buffer.length()));

                if (configuration.isOverrideContentType()) {
                    httpHeaders.set(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                }
            });
    }
}
