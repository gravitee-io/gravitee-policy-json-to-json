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
import io.gravitee.gateway.api.http.HttpHeaders;
import io.reactivex.rxjava3.core.Maybe;
import java.util.Optional;

class HttpBodyWrapper implements MessageWrapper<Buffer> {

    private final Maybe<Buffer> body;
    private final HttpHeaders headers;
    private final boolean overrideContentType;

    HttpBodyWrapper(final Maybe<Buffer> body, final HttpHeaders headers, final boolean overrideContentType) {
        this.body = body;
        this.headers = headers;
        this.overrideContentType = overrideContentType;
    }

    @Override
    public boolean isJsonContentType() {
        return Optional.ofNullable(headers.get(HttpHeaderNames.CONTENT_TYPE))
            .map(v -> v.toLowerCase().contains("json"))
            .orElse(false);
    }

    @Override
    public Maybe<Buffer> content() {
        return body;
    }

    @Override
    public Buffer withContent(final Buffer content) {
        headers.set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(content.length()));
        if (overrideContentType) {
            headers.set(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        }
        return content;
    }

    @Override
    public Maybe<Buffer> unchanged() {
        return body;
    }

    @Override
    public Maybe<Buffer> emptyContent() {
        return Maybe.empty();
    }
}
