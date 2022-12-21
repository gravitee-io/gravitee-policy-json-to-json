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

import static io.gravitee.policy.test.MessageBuilder.aMessage;
import static io.gravitee.policy.test.RequestBuilder.aRequest;
import static io.gravitee.policy.test.ResponseBuilder.aResponse;
import static org.assertj.core.api.Assertions.assertThat;

import io.gravitee.common.http.MediaType;
import io.gravitee.gateway.api.http.HttpHeaderNames;
import io.gravitee.gateway.jupiter.api.ExecutionFailure;
import io.gravitee.gateway.jupiter.core.context.interruption.InterruptionFailureException;
import io.gravitee.policy.json2json.configuration.JsonToJsonTransformationPolicyConfiguration;
import io.gravitee.policy.test.ExecutionContextBuilder;
import io.vertx.core.json.JsonObject;
import java.util.Map;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class JsonToJsonTransformationPolicyTest {

    private static final String VALID_JOLT =
        "[\n{\n\"operation\": \"shift\",\n\"spec\": {\n\"_id\": \"id\",\n\"*\": {\n\"$\": \"&1\"\n}\n}\n},\n{\n\"operation\": \"remove\",\n\"spec\": {\n\"__v\": \"\"\n}\n}\n]";

    private static final String INVALID_JOLT = "[invalid, json, file]";
    public static final String INPUT_CONTENT = "{ \"_id\": \"57762dc6ab7d620000000001\", \"name\": \"name\", \"__v\": 0}";
    public static final JsonObject EXPECTED_CONTENT = new JsonObject("{ \"id\": \"57762dc6ab7d620000000001\", \"name\": \"name\"}");

    @Nested
    class onRequest {

        @Test
        void should_apply_jolt_transformation_on_request_body_and_update_content_length() {
            var ctx = new ExecutionContextBuilder().request(aRequest().jsonBody(INPUT_CONTENT).build()).build();
            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(VALID_JOLT).overrideContentType(false).build())
                .onRequest(ctx)
                .test()
                .assertComplete();

            ctx
                .request()
                .body()
                .test()
                .assertComplete()
                .assertValue(buffer -> {
                    assertThat(new JsonObject(buffer.toString())).isEqualTo(EXPECTED_CONTENT);
                    return true;
                });
            assertThat(ctx.request().headers().toSingleValueMap())
                .contains(Map.entry(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(EXPECTED_CONTENT.toString().length())));
        }

        @Test
        void should_override_request_header_content_type_when_configured() {
            var ctx = new ExecutionContextBuilder()
                .request(aRequest().jsonBody(INPUT_CONTENT).contentType("application/vnd.anything.v1+json").build())
                .build();
            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(VALID_JOLT).overrideContentType(true).build())
                .onRequest(ctx)
                .test()
                .assertComplete();
            ctx.request().body().test().assertComplete();

            assertThat(ctx.request().headers().toSingleValueMap())
                .contains(Map.entry(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON));
        }

        @Test
        void should_not_override_request_headers_when_configured() {
            var ctx = new ExecutionContextBuilder()
                .request(aRequest().jsonBody(INPUT_CONTENT).contentType("application/vnd.anything.v1+json").build())
                .build();
            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(VALID_JOLT).overrideContentType(false).build())
                .onRequest(ctx)
                .test()
                .assertComplete();
            ctx.request().body().test().assertComplete();

            assertThat(ctx.request().headers().toSingleValueMap())
                .contains(Map.entry(HttpHeaderNames.CONTENT_TYPE, "application/vnd.anything.v1+json"));
        }

        @Test
        void should_interrupt_execution_when_jolt_spec_is_invalid() {
            var ctx = new ExecutionContextBuilder().request(aRequest().jsonBody(INPUT_CONTENT).build()).build();
            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(INVALID_JOLT).build())
                .onRequest(ctx)
                .test()
                .assertError(e -> {
                    assertThat(e)
                        .isInstanceOf(InterruptionFailureException.class)
                        .extracting(error -> ((InterruptionFailureException) error).getExecutionFailure())
                        .extracting(ExecutionFailure::statusCode, ExecutionFailure::key, ExecutionFailure::message)
                        .containsExactly(500, "JSON_INVALID_SPECIFICATION", "Unable to apply JOLT transformation to payload");
                    return true;
                });
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { MediaType.TEXT_PLAIN, "" })
        void should_ignore_non_json_body(String contentType) {
            String textContent = "plain text";
            var requestBuilder = aRequest().body(textContent);
            if (contentType != null) {
                requestBuilder.contentType(contentType);
            }
            var ctx = new ExecutionContextBuilder().request(requestBuilder.build()).build();

            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(VALID_JOLT).overrideContentType(true).build())
                .onRequest(ctx)
                .test()
                .assertComplete();

            ctx
                .request()
                .body()
                .test()
                .assertComplete()
                .assertValue(buffer -> {
                    assertThat(buffer).hasToString(textContent);
                    return true;
                });
            assertThat(ctx.request().headers().toSingleValueMap())
                .contains(Map.entry(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(textContent.length())));
        }
    }

    @Nested
    class onResponse {

        @Test
        void should_apply_jolt_transformation_on_response_body_and_update_content_length() {
            var ctx = new ExecutionContextBuilder().response(aResponse().jsonBody(INPUT_CONTENT).build()).build();
            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(VALID_JOLT).overrideContentType(false).build())
                .onResponse(ctx)
                .test()
                .assertComplete();

            ctx
                .response()
                .body()
                .test()
                .assertComplete()
                .assertValue(buffer -> {
                    assertThat(new JsonObject(buffer.toString())).isEqualTo(EXPECTED_CONTENT);
                    return true;
                });
            assertThat(ctx.response().headers().toSingleValueMap())
                .contains(Map.entry(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(EXPECTED_CONTENT.toString().length())));
        }

        @Test
        void should_override_response_header_content_type_when_configured() {
            var ctx = new ExecutionContextBuilder()
                .response(aResponse().jsonBody(INPUT_CONTENT).contentType("application/vnd.anything.v1+json").build())
                .build();
            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(VALID_JOLT).overrideContentType(true).build())
                .onResponse(ctx)
                .test()
                .assertComplete();
            ctx.response().body().test().assertComplete();

            assertThat(ctx.response().headers().toSingleValueMap())
                .contains(Map.entry(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON));
        }

        @Test
        void should_not_override_response_headers_when_configured() {
            var ctx = new ExecutionContextBuilder()
                .response(aResponse().jsonBody(INPUT_CONTENT).contentType("application/vnd.anything.v1+json").build())
                .build();
            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(VALID_JOLT).overrideContentType(false).build())
                .onResponse(ctx)
                .test()
                .assertComplete();
            ctx.response().body().test().assertComplete();

            assertThat(ctx.response().headers().toSingleValueMap())
                .contains(Map.entry(HttpHeaderNames.CONTENT_TYPE, "application/vnd.anything.v1+json"));
        }

        @Test
        void should_interrupt_execution_when_jolt_spec_is_invalid() {
            var ctx = new ExecutionContextBuilder().response(aResponse().jsonBody(INPUT_CONTENT).build()).build();
            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(INVALID_JOLT).build())
                .onResponse(ctx)
                .test()
                .assertError(e -> {
                    assertThat(e)
                        .isInstanceOf(InterruptionFailureException.class)
                        .extracting(error -> ((InterruptionFailureException) error).getExecutionFailure())
                        .extracting(ExecutionFailure::statusCode, ExecutionFailure::key, ExecutionFailure::message)
                        .containsExactly(500, "JSON_INVALID_SPECIFICATION", "Unable to apply JOLT transformation to payload");
                    return true;
                });
        }

        @Test
        void should_ignore_non_json_body() {
            String textContent = "plain text";
            var ctx = new ExecutionContextBuilder()
                .response(aResponse().body(textContent).contentType(MediaType.TEXT_PLAIN).build())
                .build();

            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(VALID_JOLT).overrideContentType(true).build())
                .onResponse(ctx)
                .test()
                .assertComplete();

            ctx
                .response()
                .body()
                .test()
                .assertComplete()
                .assertValue(buffer -> {
                    assertThat(buffer).hasToString(textContent);
                    return true;
                });
            assertThat(ctx.response().headers().toSingleValueMap())
                .contains(Map.entry(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(textContent.length())))
                .contains(Map.entry(HttpHeaderNames.CONTENT_TYPE, MediaType.TEXT_PLAIN));
        }
    }

    @Nested
    class onMessageRequest {

        @Test
        void should_apply_jolt_transformation_on_all_request_messages_and_update_content_length() {
            var ctx = new ExecutionContextBuilder()
                .request(aRequest().messages(aMessage().content(INPUT_CONTENT).build(), aMessage().content(INPUT_CONTENT).build()).build())
                .build();

            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(VALID_JOLT).overrideContentType(true).build())
                .onMessageRequest(ctx)
                .test()
                .assertComplete();

            var messages = ctx.request().messages().test().assertComplete().values();
            assertThat(messages)
                .hasSize(2)
                .allSatisfy(message -> {
                    assertThat(new JsonObject(message.content().toString())).isEqualTo(EXPECTED_CONTENT);
                    assertThat(message.headers().toSingleValueMap())
                        .contains(Map.entry(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(EXPECTED_CONTENT.toString().length())));
                });
        }

        @Test
        void should_override_message_content_type_header_when_configured() {
            var ctx = new ExecutionContextBuilder()
                .request(
                    aRequest().messages(aMessage().content(INPUT_CONTENT).build()).contentType("application/vnd.anything.v1+json").build()
                )
                .build();

            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(VALID_JOLT).overrideContentType(true).build())
                .onMessageRequest(ctx)
                .test()
                .assertComplete();

            var messages = ctx.request().messages().test().assertComplete().values();
            assertThat(messages)
                .hasSize(1)
                .allSatisfy(message ->
                    assertThat(message.headers().toSingleValueMap())
                        .contains(Map.entry(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                );
        }

        @Test
        void should_not_override_request_headers_when_configured() {
            var ctx = new ExecutionContextBuilder()
                .request(
                    aRequest().messages(aMessage().content(INPUT_CONTENT).contentType("application/vnd.anything.v1+json").build()).build()
                )
                .build();

            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(VALID_JOLT).overrideContentType(false).build())
                .onMessageRequest(ctx)
                .test()
                .assertComplete();

            var messages = ctx.request().messages().test().assertComplete().values();
            assertThat(messages)
                .hasSize(1)
                .allSatisfy(message ->
                    assertThat(message.headers().toSingleValueMap())
                        .contains(Map.entry(HttpHeaderNames.CONTENT_TYPE, "application/vnd.anything.v1+json"))
                );
        }

        @Test
        void should_interrupt_execution_when_jolt_spec_is_invalid() {
            var ctx = new ExecutionContextBuilder().request(aRequest().messages(aMessage().content(INPUT_CONTENT).build()).build()).build();

            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(INVALID_JOLT).build())
                .onMessageRequest(ctx)
                .test()
                .assertComplete();

            ctx
                .request()
                .messages()
                .test()
                .assertError(e -> {
                    assertThat(e)
                        .isInstanceOf(InterruptionFailureException.class)
                        .extracting(error -> ((InterruptionFailureException) error).getExecutionFailure())
                        .extracting(ExecutionFailure::statusCode, ExecutionFailure::key, ExecutionFailure::message)
                        .containsExactly(500, "JSON_INVALID_SPECIFICATION", "Unable to apply JOLT transformation to payload");
                    return true;
                });
        }

        @Test
        void should_ignore_null_messages() {
            var ctx = new ExecutionContextBuilder()
                .request(aRequest().messages(null, aMessage().content(INPUT_CONTENT).build(), null).build())
                .build();

            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(VALID_JOLT).overrideContentType(true).build())
                .onMessageRequest(ctx)
                .test()
                .assertComplete();

            ctx.request().messages().test().assertComplete().assertValueCount(1);
        }

        @Test
        void should_emit_original_message_when_no_content() {
            var originalMessage = aMessage().content("").build();
            var ctx = new ExecutionContextBuilder().request(aRequest().messages(originalMessage).build()).build();

            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(VALID_JOLT).overrideContentType(true).build())
                .onMessageRequest(ctx)
                .test()
                .assertComplete();

            ctx.request().messages().test().assertComplete().assertValue(originalMessage);
        }
    }

    @Nested
    class onMessageResponse {

        @Test
        void should_apply_jolt_transformation_on_all_response_messages_and_update_content_length() {
            var ctx = new ExecutionContextBuilder()
                .response(
                    aResponse().messages(aMessage().content(INPUT_CONTENT).build(), aMessage().content(INPUT_CONTENT).build()).build()
                )
                .build();

            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(VALID_JOLT).overrideContentType(true).build())
                .onMessageResponse(ctx)
                .test()
                .assertComplete();

            var messages = ctx.response().messages().test().assertComplete().values();
            assertThat(messages)
                .hasSize(2)
                .allSatisfy(message -> {
                    assertThat(new JsonObject(message.content().toString())).isEqualTo(EXPECTED_CONTENT);
                    assertThat(message.headers().toSingleValueMap())
                        .contains(Map.entry(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(EXPECTED_CONTENT.toString().length())));
                });
        }

        @Test
        void should_override_message_content_type_header_when_configured() {
            var ctx = new ExecutionContextBuilder()
                .response(
                    aResponse().messages(aMessage().content(INPUT_CONTENT).build()).contentType("application/vnd.anything.v1+json").build()
                )
                .build();

            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(VALID_JOLT).overrideContentType(true).build())
                .onMessageResponse(ctx)
                .test()
                .assertComplete();

            var messages = ctx.response().messages().test().assertComplete().values();
            assertThat(messages)
                .hasSize(1)
                .allSatisfy(message ->
                    assertThat(message.headers().toSingleValueMap())
                        .contains(Map.entry(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                );
        }

        @Test
        void should_not_override_request_headers_when_configured() {
            var ctx = new ExecutionContextBuilder()
                .response(
                    aResponse().messages(aMessage().content(INPUT_CONTENT).contentType("application/vnd.anything.v1+json").build()).build()
                )
                .build();

            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(VALID_JOLT).overrideContentType(false).build())
                .onMessageResponse(ctx)
                .test()
                .assertComplete();

            var messages = ctx.response().messages().test().assertComplete().values();
            assertThat(messages)
                .hasSize(1)
                .allSatisfy(message ->
                    assertThat(message.headers().toSingleValueMap())
                        .contains(Map.entry(HttpHeaderNames.CONTENT_TYPE, "application/vnd.anything.v1+json"))
                );
        }

        @Test
        void should_interrupt_execution_when_jolt_spec_is_invalid() {
            var ctx = new ExecutionContextBuilder()
                .response(aResponse().messages(aMessage().content(INPUT_CONTENT).build()).build())
                .build();

            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(INVALID_JOLT).build())
                .onMessageResponse(ctx)
                .test()
                .assertComplete();

            ctx
                .response()
                .messages()
                .test()
                .assertError(e -> {
                    assertThat(e)
                        .isInstanceOf(InterruptionFailureException.class)
                        .extracting(error -> ((InterruptionFailureException) error).getExecutionFailure())
                        .extracting(ExecutionFailure::statusCode, ExecutionFailure::key, ExecutionFailure::message)
                        .containsExactly(500, "JSON_INVALID_SPECIFICATION", "Unable to apply JOLT transformation to payload");
                    return true;
                });
        }

        @Test
        void should_ignore_null_messages() {
            var ctx = new ExecutionContextBuilder()
                .response(aResponse().messages(null, aMessage().content(INPUT_CONTENT).build(), null).build())
                .build();

            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(VALID_JOLT).overrideContentType(true).build())
                .onMessageResponse(ctx)
                .test()
                .assertComplete();

            ctx.response().messages().test().assertComplete().assertValueCount(1);
        }

        @Test
        void should_emit_original_message_when_no_content() {
            var originalMessage = aMessage().content("").build();
            var ctx = new ExecutionContextBuilder().response(aResponse().messages(originalMessage).build()).build();

            policy(JsonToJsonTransformationPolicyConfiguration.builder().specification(VALID_JOLT).overrideContentType(true).build())
                .onMessageResponse(ctx)
                .test()
                .assertComplete();

            ctx.response().messages().test().assertComplete().assertValue(originalMessage);
        }
    }

    JsonToJsonTransformationPolicy policy(JsonToJsonTransformationPolicyConfiguration configuration) {
        return new JsonToJsonTransformationPolicy(configuration);
    }
}
