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
import io.gravitee.gateway.jupiter.api.message.DefaultMessage;
import io.gravitee.gateway.jupiter.api.message.Message;

public class MessageBuilder {

    private Buffer content = Buffer.buffer();
    private final HttpHeaders headers = HttpHeaders.create();

    public static MessageBuilder aMessage() {
        return new MessageBuilder();
    }

    public MessageBuilder content(String body) {
        this.content = Buffer.buffer(body);
        this.headers.set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(this.content.length()));
        return this;
    }

    public MessageBuilder contentType(String contentType) {
        return header(HttpHeaderNames.CONTENT_TYPE, contentType);
    }

    public MessageBuilder header(String name, String value) {
        this.headers.set(name, value);
        return this;
    }

    public Message build() {
        var message = new DefaultMessage();
        message.content(content);
        message.headers(headers);
        return message;
    }
}
