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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;
import static org.assertj.core.api.Assertions.assertThat;

import io.gravitee.apim.gateway.tests.sdk.annotations.DeployApi;
import io.gravitee.apim.gateway.tests.sdk.annotations.GatewayTest;
import io.gravitee.common.http.MediaType;
import io.gravitee.gateway.api.http.HttpHeaderNames;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.http.HttpClient;
import io.vertx.rxjava3.core.http.HttpClientRequest;
import io.vertx.rxjava3.core.http.HttpClientResponse;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class JsonToJsonTransformationPolicyV4EngineIntegrationTest {

    @Nested
    @GatewayTest
    @DeployApi(
        {
            "/apis/v2/pre_valid_jolt_spec_with_el.json",
            "/apis/v2/pre_invalid_jolt_spec.json",
            "/apis/v4/request_invalid_jolt_spec.json",
            "/apis/v4/request_valid_jolt_spec_with_el.json",
        }
    )
    class OnRequest extends V4EngineTest {

        @ParameterizedTest
        @ValueSource(strings = { "/pre-jolt-spec-with-el", "/request-jolt-spec-with-el" })
        public void should_apply_the_transformation_with_EL_on_the_request_body(String requestUri, HttpClient client) throws Exception {
            var input = load("/io/gravitee/policy/json2json/input01.json");
            var expected = load("/io/gravitee/policy/json2json/expected02.json");

            wiremock.stubFor(post("/team").willReturn(ok()));

            client
                .rxRequest(POST, requestUri)
                .flatMap(request -> request.rxSend(input.toString()))
                .flatMapPublisher(response -> {
                    assertThat(response.statusCode()).isEqualTo(200);
                    return response.toFlowable();
                })
                .test()
                .await()
                .assertComplete()
                .assertNoErrors();

            wiremock.verify(1, postRequestedFor(urlPathEqualTo("/team")).withRequestBody(equalToJson(expected.toString())));
        }

        @ParameterizedTest
        @ValueSource(strings = { "/pre-invalid-jolt", "/request-invalid-jolt-spec" })
        public void should_respond_with_500_when_applying_an_invalid_JOLT_transformation_on_the_request_body(
            String requestUri,
            HttpClient client
        ) throws Exception {
            var input = load("/io/gravitee/policy/json2json/input01.json");

            wiremock.stubFor(post("/team").withHeader("Content-Type", equalTo("application/json")).willReturn(ok()));

            client
                .rxRequest(POST, requestUri)
                .flatMap(request -> request.rxSend(input.toString()))
                .flatMapPublisher(response -> {
                    assertThat(response.statusCode()).isEqualTo(500);
                    return response.toFlowable();
                })
                .test()
                .await()
                .assertComplete()
                .assertNoErrors();
        }
    }

    @Nested
    @GatewayTest
    @DeployApi(
        {
            "/apis/v2/post_valid_jolt_spec_with_el.json",
            "/apis/v2/post_invalid_jolt_spec.json",
            "/apis/v4/response_valid_jolt_spec_with_el.json",
            "/apis/v4/response_invalid_jolt_spec.json",
        }
    )
    class OnResponse extends V4EngineTest {

        @ParameterizedTest
        @ValueSource(strings = { "/post-jolt-spec-with-el", "/response-jolt-spec-with-el" })
        public void should_apply_the_transformation_with_EL_on_the_response_body(String requestUri, HttpClient client) throws Exception {
            var backendResponse = load("/io/gravitee/policy/json2json/input01.json");
            var expected = load("/io/gravitee/policy/json2json/expected02.json");

            wiremock.stubFor(get("/team").willReturn(ok(backendResponse.toString())));

            client
                .rxRequest(GET, requestUri)
                .flatMap(HttpClientRequest::rxSend)
                .flatMapPublisher(response -> {
                    assertThat(response.statusCode()).isEqualTo(200);
                    return response.toFlowable();
                })
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()
                .assertValue(result -> {
                    assertThat(result.toJsonObject()).isEqualTo(expected);
                    return true;
                });
        }

        @ParameterizedTest
        @ValueSource(strings = { "/post-invalid-jolt", "/response-invalid-jolt-spec" })
        public void should_respond_with_500_when_applying_an_invalid_JOLT_transformation_on_the_response_body(
            String requestUri,
            HttpClient client
        ) throws Exception {
            var backendResponse = load("/io/gravitee/policy/json2json/input01.json");

            wiremock.stubFor(get("/team").willReturn(ok(backendResponse.toString())));

            client
                .rxRequest(GET, requestUri)
                .flatMap(HttpClientRequest::rxSend)
                .flatMapPublisher(response -> {
                    assertThat(response.statusCode()).isEqualTo(500);
                    return response.toFlowable();
                })
                .test()
                .await()
                .assertComplete()
                .assertNoErrors();
        }
    }

    @Nested
    @GatewayTest
    @DeployApi({ "/apis/v4/subscribe_invalid_jolt_spec.json", "/apis/v4/subscribe_valid_jolt_spec_with_el.json" })
    class OnResponseMessage extends V4EngineTest {

        @Test
        public void should_apply_the_transformation_with_EL_on_the_response_message(HttpClient client) {
            var expected = load("/io/gravitee/policy/json2json/expected02.json");

            client
                .rxRequest(HttpMethod.GET, "/subscribe-jolt-spec-with-el")
                .flatMap(request -> {
                    request.putHeader(HttpHeaderNames.ACCEPT.toString(), MediaType.TEXT_EVENT_STREAM);
                    return request.rxSend();
                })
                .flatMapPublisher(HttpClientResponse::toFlowable)
                .map(Buffer::toString)
                .filter(content -> !content.startsWith("retry")) // ignore retry
                .filter(content -> !content.equals(":\n\n")) // ignore heartbeat
                .test()
                .awaitCount(1)
                .assertValue(content -> {
                    assertThat(content).contains("event: message").contains("data: " + expected);
                    return true;
                });
        }

        @Test
        public void should_respond_with_500_when_applying_an_invalid_JOLT_transformation_on_the_response_message(HttpClient client) {
            client
                .rxRequest(GET, "/subscribe-invalid-jolt-spec")
                .flatMap(request -> {
                    request.putHeader(HttpHeaderNames.ACCEPT.toString(), MediaType.TEXT_EVENT_STREAM);
                    return request.rxSend();
                })
                .flatMapPublisher(HttpClientResponse::toFlowable)
                .map(Buffer::toString)
                .filter(content -> !content.startsWith("retry")) // ignore retry
                .filter(content -> !content.equals(":\n\n")) // ignore heartbeat
                .test()
                .awaitCount(1)
                .assertValue(content -> {
                    assertThat(content).contains("event: error").contains("data: Unable to apply JOLT transformation to payload");
                    return true;
                });
        }
    }

    private JsonObject load(String resourcePath) {
        try (InputStream is = this.getClass().getResourceAsStream(resourcePath)) {
            return new JsonObject(new String(Objects.requireNonNull(is).readAllBytes(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            return new JsonObject();
        }
    }
}
