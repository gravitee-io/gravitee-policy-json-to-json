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
package io.gravitee.policy.test;

import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.HttpHeaderNames;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.jupiter.api.message.Message;
import io.gravitee.gateway.jupiter.core.context.AbstractResponse;
import io.gravitee.gateway.jupiter.core.context.MutableResponse;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ResponseBuilder {

    private Buffer body = Buffer.buffer();
    private Flowable<Message> messages = Flowable.empty();
    private final HttpHeaders headers = HttpHeaders.create();

    public static ResponseBuilder aResponse() {
        return new ResponseBuilder();
    }

    public ResponseBuilder body(String body) {
        this.body = Buffer.buffer(body);
        this.headers.set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(this.body.length()));
        return this;
    }

    public ResponseBuilder messages(Message... messages) {
        this.messages =
            Flowable.concat(Arrays.stream(messages).map(m -> Maybe.fromCallable(() -> m).toFlowable()).collect(Collectors.toList()));
        return this;
    }

    public ResponseBuilder contentType(String contentType) {
        return header(HttpHeaderNames.CONTENT_TYPE, contentType);
    }

    public ResponseBuilder header(String name, String value) {
        this.headers.set(name, value);
        return this;
    }

    public MutableResponse build() {
        var response = new FakeResponse();
        response.body(body);
        response.headers(headers);
        response.messages(messages);
        return response;
    }

    public static class FakeResponse extends AbstractResponse {

        public FakeResponse() {}

        public void headers(HttpHeaders headers) {
            this.headers = headers;
        }
    }
}
