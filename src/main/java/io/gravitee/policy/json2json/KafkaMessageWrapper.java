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

import io.gravitee.common.http.MediaType;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.HttpHeaderNames;
import io.gravitee.gateway.reactive.api.message.kafka.KafkaMessage;
import io.reactivex.rxjava3.core.Maybe;
import java.util.Optional;

class KafkaMessageWrapper implements MessageWrapper<KafkaMessage> {

    private final KafkaMessage message;
    private final boolean overrideContentType;

    KafkaMessageWrapper(final KafkaMessage message, final boolean overrideContentType) {
        this.message = message;
        this.overrideContentType = overrideContentType;
    }

    @Override
    public boolean isJsonContentType() {
        return Optional.ofNullable(message.recordHeaders().get(HttpHeaderNames.CONTENT_TYPE))
            .map(Buffer::toString)
            .map(v -> v.toLowerCase().contains("json"))
            .orElse(true);
    }

    @Override
    public Maybe<Buffer> content() {
        return Maybe.fromCallable(message::content);
    }

    @Override
    public KafkaMessage withContent(final Buffer content) {
        message.putRecordHeader(HttpHeaderNames.CONTENT_LENGTH, Buffer.buffer(content.length()));
        if (overrideContentType) {
            message.putRecordHeader(HttpHeaderNames.CONTENT_TYPE, Buffer.buffer(MediaType.APPLICATION_JSON));
        }
        return (KafkaMessage) message.content(content);
    }

    @Override
    public Maybe<KafkaMessage> unchanged() {
        return Maybe.just(message);
    }

    @Override
    public Maybe<KafkaMessage> emptyContent() {
        return Maybe.just(message);
    }
}
