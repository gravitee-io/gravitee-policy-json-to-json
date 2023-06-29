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

import com.bazaarvoice.jolt.JsonUtils;
import io.gravitee.apim.gateway.tests.sdk.annotations.DeployApi;
import io.gravitee.apim.gateway.tests.sdk.annotations.GatewayTest;
import io.gravitee.common.http.MediaType;
import io.gravitee.gateway.api.http.HttpHeaderNames;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.http.HttpClient;
import io.vertx.rxjava3.core.http.HttpClientRequest;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.junit.jupiter.api.*;

/**
 * @author Yann TAVERNIER (yann.tavernier at graviteesource.com)
 * @author GraviteeSource Team
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class JsonToJsonTransformationPolicyV3EngineIntegrationTest {

    @Nested
    @DeployApi({ "/apis/v2/pre_valid_jolt_spec.json", "/apis/v2/pre_invalid_jolt_spec.json", "/apis/v2/pre_valid_jolt_spec_with_el.json" })
    class onRequestContent extends V3EngineTest {

        @Test
        public void should_apply_the_transformation_on_the_request_to_the_backend(HttpClient client) throws Exception {
            String input = loadResource("/io/gravitee/policy/json2json/input01.json");
            String expected = loadResource("/io/gravitee/policy/json2json/expected01.json");

            wiremock.stubFor(post("/team").withHeader("Content-Type", equalTo("application/json")).willReturn(ok()));

            client
                .rxRequest(POST, "/pre-valid-jolt-spec")
                .map(r -> r.putHeader(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .flatMap(request -> request.rxSend(Buffer.buffer(input)))
                .flatMapPublisher(response -> {
                    assertThat(response.statusCode()).isEqualTo(200);
                    return response.toFlowable();
                })
                .test()
                .await()
                .assertComplete()
                .assertNoErrors();

            wiremock.verify(1, postRequestedFor(urlPathEqualTo("/team")).withRequestBody(equalToJson(expected)));
        }

        @Test
        public void should_respond_with_500_when_applying_an_invalid_jolt_transformation_on_the_request_to_the_backed(HttpClient client)
            throws Exception {
            String input = loadResource("/io/gravitee/policy/json2json/input01.json");

            wiremock.stubFor(post("/team").withHeader("Content-Type", equalTo("application/json")).willReturn(ok()));

            client
                .rxRequest(POST, "/pre-invalid-jolt")
                .map(r -> r.putHeader(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .flatMap(request -> request.rxSend(Buffer.buffer(input)))
                .flatMapPublisher(response -> {
                    assertThat(response.statusCode()).isEqualTo(500);
                    return response.toFlowable();
                })
                .test()
                .await()
                .assertComplete()
                .assertNoErrors();
        }

        @Test
        public void should_apply_the_transformation_with_el_on_the_request_to_the_backend(HttpClient client) throws Exception {
            String input = loadResource("/io/gravitee/policy/json2json/input01.json");
            String expected = loadResource("/io/gravitee/policy/json2json/expected02.json");

            wiremock.stubFor(post("/team").withHeader("Content-Type", equalTo("application/json")).willReturn(ok()));

            client
                .rxRequest(POST, "/pre-jolt-spec-with-el")
                .map(r -> r.putHeader(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .flatMap(request -> request.rxSend(Buffer.buffer(input)))
                .flatMapPublisher(response -> {
                    assertThat(response.statusCode()).isEqualTo(200);
                    return response.toFlowable();
                })
                .test()
                .await()
                .assertComplete()
                .assertNoErrors();

            wiremock.verify(1, postRequestedFor(urlPathEqualTo("/team")).withRequestBody(equalToJson(expected)));
        }
    }

    @Nested
    @DeployApi(
        { "/apis/v2/post_invalid_jolt_spec.json", "/apis/v2/post_valid_jolt_spec.json", "/apis/v2/post_valid_jolt_spec_with_el.json" }
    )
    class onResponseContent extends V3EngineTest {

        @Test
        public void should_apply_the_transformation_on_the_response_of_the_backend(HttpClient client) throws Exception {
            String backendResponse = loadResource("/io/gravitee/policy/json2json/input01.json");
            String expected = loadResource("/io/gravitee/policy/json2json/expected01.json");

            wiremock.stubFor(
                get("/team").willReturn(ok(backendResponse).withHeader(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON))
            );

            client
                .rxRequest(GET, "/post-valid-jolt-spec")
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
                    assertThat(JsonUtils.javason(result.toString())).isEqualTo(JsonUtils.javason(expected));
                    return true;
                });

            wiremock.verify(1, getRequestedFor(urlPathEqualTo("/team")));
        }

        @Test
        public void should_apply_the_transformation_with_el_on_the_response_of_the_backend(HttpClient client) throws Exception {
            String backendResponse = loadResource("/io/gravitee/policy/json2json/input01.json");
            String expected = loadResource("/io/gravitee/policy/json2json/expected02.json");

            wiremock.stubFor(
                get("/team").willReturn(ok(backendResponse).withHeader(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON))
            );

            client
                .rxRequest(GET, "/post-jolt-spec-with-el")
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
                    assertThat(JsonUtils.javason(result.toString())).isEqualTo(JsonUtils.javason(expected));
                    return true;
                });

            wiremock.verify(1, getRequestedFor(urlPathEqualTo("/team")));
        }

        @Test
        public void should_respond_with_500_when_applying_an_invalid_jolt_transformation_on_the_response_of_the_backed(HttpClient client)
            throws Exception {
            String backendResponse = loadResource("/io/gravitee/policy/json2json/input01.json");

            wiremock.stubFor(
                get("/team").willReturn(ok(backendResponse).withHeader(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON))
            );

            client
                .rxRequest(GET, "/post-invalid-jolt")
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

    private String loadResource(String resource) {
        try (InputStream is = this.getClass().getResourceAsStream(resource)) {
            return new String(Objects.requireNonNull(is).readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }
}
