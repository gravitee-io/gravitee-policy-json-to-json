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
package io.gravitee.policy.test;

import io.gravitee.gateway.reactive.api.message.kafka.KafkaMessage;

/**
 * Simulates the Kafka reactor's EvaluableMessage, which exposes getTopic() in addition to
 * the standard getContent(). Used in unit tests to verify that AssignContentPolicy correctly
 * delegates to the framework's template engine binding rather than constructing its own
 * EvaluableMessage (which lacks getTopic()).
 */
public class EvaluableKafkaMessageStub {

    private final KafkaMessage message;

    public EvaluableKafkaMessageStub(KafkaMessage message) {
        this.message = message;
    }

    public String getContent() {
        return message.content().toString();
    }

    public String getTopic() {
        return message.topic();
    }
}
