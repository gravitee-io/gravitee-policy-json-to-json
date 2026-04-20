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
import io.gravitee.gateway.reactive.api.message.Message;
import io.reactivex.rxjava3.core.Maybe;
import java.util.Optional;

class HttpMessageWrapper<T extends Message> implements MessageWrapper<T> {

    private final T message;
    private final boolean overrideContentType;

    HttpMessageWrapper(final T message, final boolean overrideContentType) {
        this.message = message;
        this.overrideContentType = overrideContentType;
    }

    @Override
    public boolean isJsonContentType() {
        return Optional.ofNullable(message.headers().get(HttpHeaderNames.CONTENT_TYPE))
            .map(v -> v.toLowerCase().contains("json"))
            .orElse(true);
    }

    @Override
    public Maybe<Buffer> content() {
        return Maybe.fromCallable(message::content);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withContent(final Buffer content) {
        message.headers().set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(content.length()));
        if (overrideContentType) {
            message.headers().set(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        }
        return (T) message.content(content);
    }

    @Override
    public Maybe<T> unchanged() {
        return Maybe.just(message);
    }

    @Override
    public Maybe<T> emptyContent() {
        return Maybe.just(message);
    }
}
