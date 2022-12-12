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
import io.gravitee.apim.gateway.tests.sdk.AbstractPolicyTest;
import io.gravitee.apim.gateway.tests.sdk.annotations.DeployApi;
import io.gravitee.apim.gateway.tests.sdk.annotations.GatewayTest;
import io.gravitee.policy.json2json.configuration.JsonToJsonTransformationPolicyConfiguration;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.http.HttpClient;
import io.vertx.rxjava3.core.http.HttpClientRequest;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author Yann TAVERNIER (yann.tavernier at graviteesource.com)
 * @author GraviteeSource Team
 */
@GatewayTest
public class JsonToJsonTransformationPolicyIntegrationTest
    extends AbstractPolicyTest<JsonToJsonTransformationPolicy, JsonToJsonTransformationPolicyConfiguration> {

    @Test
    @DisplayName("Should apply the transformation on the request to the backend")
    @DeployApi("/apis/api-pre.json")
    public void shouldTransformOnRequestContent(HttpClient client) throws Exception {
        String input = loadResource("/io/gravitee/policy/json2json/input01.json");
        String expected = loadResource("/io/gravitee/policy/json2json/expected01.json");

        wiremock.stubFor(post("/team").withHeader("Content-Type", equalTo("application/json")).willReturn(ok()));

        client
            .rxRequest(POST, "/test")
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
    @DisplayName("Should respond with 500 when applying an invalid JOLT transformation on the request to the backed")
    @DeployApi("/apis/api-invalid-pre.json")
    public void shouldNotTransformOnRequestContent(HttpClient client) throws Exception {
        String input = loadResource("/io/gravitee/policy/json2json/input01.json");

        wiremock.stubFor(post("/team").withHeader("Content-Type", equalTo("application/json")).willReturn(ok()));

        client
            .rxRequest(POST, "/test")
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
    @DisplayName("Should apply the transformation with EL on the request to the backend")
    @DeployApi("/apis/api-with-el-pre.json")
    public void shouldTransformWithELOnRequestContent(HttpClient client) throws Exception {
        String input = loadResource("/io/gravitee/policy/json2json/input01.json");
        String expected = loadResource("/io/gravitee/policy/json2json/expected02.json");

        wiremock.stubFor(post("/team").withHeader("Content-Type", equalTo("application/json")).willReturn(ok()));

        client
            .rxRequest(POST, "/test")
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
    @DisplayName("Should apply the transformation on the response of the backend")
    @DeployApi("/apis/api-post.json")
    public void shouldTransformOnResponseContent(HttpClient client) throws Exception {
        String backendResponse = loadResource("/io/gravitee/policy/json2json/input01.json");
        String expected = loadResource("/io/gravitee/policy/json2json/expected01.json");

        wiremock.stubFor(get("/team").willReturn(ok(backendResponse)));

        client
            .rxRequest(GET, "/test")
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
    @DisplayName("Should apply the transformation with EL on the response of the backend")
    @DeployApi("/apis/api-with-el-post.json")
    public void shouldTransformWithELOnResponseContent(HttpClient client) throws Exception {
        String backendResponse = loadResource("/io/gravitee/policy/json2json/input01.json");
        String expected = loadResource("/io/gravitee/policy/json2json/expected02.json");

        wiremock.stubFor(get("/team").willReturn(ok(backendResponse)));

        client
            .rxRequest(GET, "/test")
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
    @DisplayName("Should respond with 500 when applying an invalid JOLT transformation on the response of the backed")
    @DeployApi("/apis/api-invalid-post.json")
    public void shouldNotTransformOnResponseContent(HttpClient client) throws Exception {
        String backendResponse = loadResource("/io/gravitee/policy/json2json/input01.json");

        wiremock.stubFor(get("/team").willReturn(ok(backendResponse)));

        client
            .rxRequest(GET, "/test")
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

    private String loadResource(String resource) {
        try (InputStream is = this.getClass().getResourceAsStream(resource)) {
            return new String(Objects.requireNonNull(is).readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }
}
