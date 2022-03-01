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

import io.gravitee.el.TemplateContext;
import io.gravitee.el.TemplateEngine;
import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.stream.exception.TransformationException;
import io.gravitee.policy.json2json.configuration.JsonToJsonTransformationPolicyConfiguration;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@ExtendWith(MockitoExtension.class)
public class JsonToJsonTransformationPolicyTest {

    @Mock
    private JsonToJsonTransformationPolicyConfiguration jsonToJsonTransformationPolicyConfiguration;

    private JsonToJsonTransformationPolicy jsonToJsonTransformationPolicy;

    @Mock
    protected ExecutionContext executionContext;

    @BeforeEach
    public void init() {
        jsonToJsonTransformationPolicy = new JsonToJsonTransformationPolicy(jsonToJsonTransformationPolicyConfiguration);
    }

    @Test
    @DisplayName("Should transform input using transformation")
    public void shouldTransformInput() throws Exception {
        String stylesheet = loadResource("/io/gravitee/policy/json2json/valid-specification.json");
        String input = loadResource("/io/gravitee/policy/json2json/input01.json");
        String expected = loadResource("/io/gravitee/policy/json2json/expected01.json");

        // Prepare context
        when(jsonToJsonTransformationPolicyConfiguration.getSpecification()).thenReturn(stylesheet);
        when(executionContext.getTemplateEngine()).thenReturn(new MockTemplateEngine());

        Buffer ret = jsonToJsonTransformationPolicy.map(executionContext).apply(Buffer.buffer(input));
        assertThat(ret).isNotNull();

        JSONAssert.assertEquals(expected, ret.toString(), false);
    }

    @Test
    @DisplayName("Should throw exception when trying to map with an invalid transformation")
    public void shouldThrowExceptionForInvalidTransformation() throws Exception {
        String specification = loadResource("/io/gravitee/policy/json2json/invalid-specification.json");
        String input = loadResource("/io/gravitee/policy/json2json/input01.json");

        // Prepare context
        when(jsonToJsonTransformationPolicyConfiguration.getSpecification()).thenReturn(specification);
        when(executionContext.getTemplateEngine()).thenReturn(new MockTemplateEngine());

        Assertions.assertThrows(TransformationException.class, () -> jsonToJsonTransformationPolicy.map(executionContext).apply(Buffer.buffer(input)));
    }

    private String loadResource(String resource) throws IOException {
        InputStream is = this.getClass().getResourceAsStream(resource);
        StringWriter sw = new StringWriter();
        IOUtils.copy(is, sw, "UTF-8");
        return sw.toString();
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
