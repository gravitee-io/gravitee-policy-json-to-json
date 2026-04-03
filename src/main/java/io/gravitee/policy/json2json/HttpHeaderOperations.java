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

import io.gravitee.gateway.api.http.HttpHeaderNames;
import io.gravitee.gateway.api.http.HttpHeaders;
import java.util.Optional;

class HttpHeaderOperations implements HeaderOperations {

    private final HttpHeaders httpHeaders;

    public HttpHeaderOperations(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    @Override
    public boolean isJsonContentType() {
        return Optional.ofNullable(httpHeaders.get(HttpHeaderNames.CONTENT_TYPE))
            .map(v -> v.toLowerCase().contains("json"))
            .orElse(false);
    }

    @Override
    public void setContentLength(int length) {
        httpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(length));
    }

    @Override
    public void setContentType(String type) {
        httpHeaders.set(HttpHeaderNames.CONTENT_TYPE, type);
    }
}
