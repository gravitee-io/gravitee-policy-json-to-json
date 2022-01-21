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

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import io.gravitee.el.TemplateContext;
import io.gravitee.el.TemplateEngine;
import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.stream.exception.TransformationException;
import io.gravitee.policy.json2json.configuration.JsonToJsonTransformationPolicyConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonToJsonTransformationPolicyTest {

    @Mock
    private JsonToJsonTransformationPolicyConfiguration jsonToJsonTransformationPolicyConfiguration;

    private JsonToJsonTransformationPolicy jsonToJsonTransformationPolicy;

    @Mock
    protected ExecutionContext executionContext;

    @Before
    public void init() {
        initMocks(this);

        jsonToJsonTransformationPolicy = new JsonToJsonTransformationPolicy(jsonToJsonTransformationPolicyConfiguration);
    }

    @Test
    public void shouldTransformInput() throws Exception {
        String stylesheet = loadResource("/io/gravitee/policy/json2json/specification01.json");
        String input = loadResource("/io/gravitee/policy/json2json/input01.json");
        String expected = loadResource("/io/gravitee/policy/json2json/expected01.json");

        // Prepare context
        when(jsonToJsonTransformationPolicyConfiguration.getSpecification()).thenReturn(stylesheet);
        when(executionContext.getTemplateEngine()).thenReturn(new MockTemplateEngine());

        Buffer ret = jsonToJsonTransformationPolicy.map(executionContext).apply(Buffer.buffer(input));
        Assert.assertNotNull(ret);

        JSONAssert.assertEquals(expected, ret.toString(), false);
    }

    @Test(expected = TransformationException.class)
    public void shouldThrowExceptionForInvalidStylesheet() throws Exception {
        String specification = loadResource("/io/gravitee/policy/json2json/specification02.json");
        String input = loadResource("/io/gravitee/policy/json2json/input01.json");

        // Prepare context
        when(jsonToJsonTransformationPolicyConfiguration.getSpecification()).thenReturn(specification);
        when(executionContext.getTemplateEngine()).thenReturn(new MockTemplateEngine());

        jsonToJsonTransformationPolicy.map(executionContext).apply(Buffer.buffer(input));
    }

    /*
    @Test(expected = TransformationException.class)
    public void shouldThrowExceptionForExternalEntityInjection() throws Exception {
        String stylesheet = loadResource("/io/gravitee/policy/xslt/specification01.json");
        String xml = loadResource("/io/gravitee/policy/xslt/file02.xml");

        // Prepare context
        when(xsltTransformationPolicyConfiguration.getStylesheet()).thenReturn(stylesheet);
        when(executionContext.getTemplateEngine()).thenReturn(new MockTemplateEngine());

        xsltTransformationPolicy.toXSLT(executionContext).apply(Buffer.buffer(xml));
    }
*/

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
