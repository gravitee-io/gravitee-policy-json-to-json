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

import com.bazaarvoice.jolt.JsonUtils;
import io.gravitee.common.http.HttpHeaders;
import io.gravitee.common.http.MediaType;
import io.gravitee.el.TemplateContext;
import io.gravitee.el.TemplateEngine;
import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.stream.ReadWriteStream;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.json2json.configuration.JsonToJsonTransformationPolicyConfiguration;
import io.gravitee.policy.json2json.configuration.PolicyScope;
import io.gravitee.reporter.api.http.Metrics;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Yann TAVERNIER (yann.tavernier at graviteesource.com)
 * @author GraviteeSource Team
 */
@ExtendWith(MockitoExtension.class)
public class JsonToJsonTransformationPolicyIntegrationTest {

    @Mock
    private JsonToJsonTransformationPolicyConfiguration jsonToJsonTransformationPolicyConfiguration;

    private JsonToJsonTransformationPolicy jsonToJsonTransformationPolicy;

    @Mock
    protected ExecutionContext executionContext;

    @Mock
    private PolicyChain policyChain;

    @Spy
    private Request request;

    @Spy
    private Response response;

    @BeforeEach
    public void init() {
        jsonToJsonTransformationPolicy = new JsonToJsonTransformationPolicy(jsonToJsonTransformationPolicyConfiguration);
        when(executionContext.getTemplateEngine()).thenReturn(new MockTemplateEngine());
    }

    @Test
    @DisplayName("Should transform and add header OnRequestContent")
    public void shouldTransformAndAddHeadersOnRequestContent() throws Exception {
        String specification = loadResource("/io/gravitee/policy/json2json/valid-specification.json");
        String input = loadResource("/io/gravitee/policy/json2json/input01.json");
        String expected = loadResource("/io/gravitee/policy/json2json/expected01.json");

        // Prepare context
        when(jsonToJsonTransformationPolicyConfiguration.getSpecification()).thenReturn(specification);
        when(jsonToJsonTransformationPolicyConfiguration.isOverrideContentType()).thenReturn(true);
        when(jsonToJsonTransformationPolicyConfiguration.getScope()).thenReturn(PolicyScope.REQUEST);
        when(request.headers()).thenReturn(new HttpHeaders());

        final ReadWriteStream result = jsonToJsonTransformationPolicy.onRequestContent(request, policyChain, executionContext);
        assertThat(result).isNotNull();
        result.bodyHandler(resultBody -> {
            assertResultingJsonObjectsAreEquals(expected, resultBody);
        });

        result.write(Buffer.buffer(input));
        result.end();

        assertThat(request.headers()).containsKey(HttpHeaders.CONTENT_TYPE);
        assertThat(request.headers().get(HttpHeaders.CONTENT_TYPE).get(0)).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(request.headers()).doesNotContainKey(HttpHeaders.TRANSFER_ENCODING);
        assertThat(request.headers()).containsKey(HttpHeaders.CONTENT_LENGTH);
    }

    @Test
    @DisplayName("Should not transform when TransformationException thrown OnRequestContent")
    public void shouldNotTransformAndAddHeadersOnRequestContent() throws Exception {
        String specification = loadResource("/io/gravitee/policy/json2json/invalid-specification.json");
        String input = loadResource("/io/gravitee/policy/json2json/input01.json");

        // Prepare context
        when(jsonToJsonTransformationPolicyConfiguration.getSpecification()).thenReturn(specification);
        when(jsonToJsonTransformationPolicyConfiguration.isOverrideContentType()).thenReturn(true);
        when(jsonToJsonTransformationPolicyConfiguration.getScope()).thenReturn(PolicyScope.REQUEST);
        when(request.headers()).thenReturn(new HttpHeaders());
        when(request.metrics()).thenReturn(Metrics.on(Instant.now().toEpochMilli()).build());

        final ReadWriteStream result = jsonToJsonTransformationPolicy.onRequestContent(request, policyChain, executionContext);
        assertThat(result).isNotNull();

        result.write(Buffer.buffer(input));
        result.end();

        assertThat(request.headers()).doesNotContainKey(HttpHeaders.CONTENT_TYPE);
        assertThat(request.headers()).doesNotContainKey(HttpHeaders.TRANSFER_ENCODING);
        assertThat(request.headers()).doesNotContainKey(HttpHeaders.CONTENT_LENGTH);
        assertThat(request.metrics().getMessage()).isEqualTo("Unable to apply JSON to JSON transformation: Unable to unmarshal JSON to a List.");
        verify(policyChain, times(1)).streamFailWith(any());
    }

    @Test
    @DisplayName("Should transform and add header OnResponseContent")
    public void shouldTransformAndAddHeadersOnResponseContent() throws Exception {
        String specification = loadResource("/io/gravitee/policy/json2json/valid-specification.json");
        String input = loadResource("/io/gravitee/policy/json2json/input01.json");
        String expected = loadResource("/io/gravitee/policy/json2json/expected01.json");

        // Prepare context
        when(jsonToJsonTransformationPolicyConfiguration.getSpecification()).thenReturn(specification);
        when(jsonToJsonTransformationPolicyConfiguration.isOverrideContentType()).thenReturn(true);
        when(jsonToJsonTransformationPolicyConfiguration.getScope()).thenReturn(PolicyScope.RESPONSE);
        when(response.headers()).thenReturn(new HttpHeaders());

        final ReadWriteStream result = jsonToJsonTransformationPolicy.onResponseContent(response, policyChain, executionContext);
        assertThat(result).isNotNull();
        result.bodyHandler(resultBody -> {
            assertResultingJsonObjectsAreEquals(expected, resultBody);
        });

        result.write(Buffer.buffer(input));
        result.end();

        assertThat(response.headers()).containsKey(HttpHeaders.CONTENT_TYPE);
        assertThat(response.headers().get(HttpHeaders.CONTENT_TYPE).get(0)).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.headers()).doesNotContainKey(HttpHeaders.TRANSFER_ENCODING);
        assertThat(response.headers()).containsKey(HttpHeaders.CONTENT_LENGTH);
    }

    @Test
    @DisplayName("Should not transform when TransformationException thrown OnResponseContent")
    public void shouldNotTransformAndAddHeadersOnResponseContent() throws Exception {
        String specification = loadResource("/io/gravitee/policy/json2json/invalid-specification.json");
        String input = loadResource("/io/gravitee/policy/json2json/input01.json");

        // Prepare context
        when(jsonToJsonTransformationPolicyConfiguration.getSpecification()).thenReturn(specification);
        when(jsonToJsonTransformationPolicyConfiguration.isOverrideContentType()).thenReturn(true);
        when(jsonToJsonTransformationPolicyConfiguration.getScope()).thenReturn(PolicyScope.RESPONSE);
        when(response.headers()).thenReturn(new HttpHeaders());

        final ReadWriteStream result = jsonToJsonTransformationPolicy.onResponseContent(response, policyChain, executionContext);
        assertThat(result).isNotNull();

        result.write(Buffer.buffer(input));
        result.end();

        assertThat(response.headers()).doesNotContainKey(HttpHeaders.CONTENT_TYPE);
        assertThat(response.headers()).doesNotContainKey(HttpHeaders.TRANSFER_ENCODING);
        assertThat(response.headers()).doesNotContainKey(HttpHeaders.CONTENT_LENGTH);
        verify(policyChain, times(1)).streamFailWith(any());
    }

    private String loadResource(String resource) throws IOException {
        InputStream is = this.getClass().getResourceAsStream(resource);
        StringWriter sw = new StringWriter();
        IOUtils.copy(is, sw, "UTF-8");
        return sw.toString();
    }

    private void assertResultingJsonObjectsAreEquals(String expected, Object resultBody) {
        assertThat(JsonUtils.javason(resultBody.toString())).isEqualTo(JsonUtils.javason(expected));
    }

    private class MockTemplateEngine implements TemplateEngine {

        @Override
        public String convert(String s) {
            return s;
        }

        @Override
        public <T> T getValue(String expression, Class<T> clazz) {
            return null;
        }

        @Override
        public TemplateContext getTemplateContext() {
            return null;
        }
    }
}
