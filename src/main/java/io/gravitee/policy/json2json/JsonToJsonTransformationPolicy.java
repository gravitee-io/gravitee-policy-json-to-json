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

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import io.gravitee.common.http.MediaType;
import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.stream.TransformableRequestStreamBuilder;
import io.gravitee.gateway.api.http.stream.TransformableResponseStreamBuilder;
import io.gravitee.gateway.api.http.stream.TransformableStreamBuilder;
import io.gravitee.gateway.api.stream.ReadWriteStream;
import io.gravitee.gateway.api.stream.exception.TransformationException;
import io.gravitee.policy.api.annotations.OnRequestContent;
import io.gravitee.policy.api.annotations.OnResponseContent;
import io.gravitee.policy.json2json.configuration.JsonToJsonTransformationPolicyConfiguration;
import io.gravitee.policy.json2json.configuration.PolicyScope;
import java.util.List;
import java.util.function.Function;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class JsonToJsonTransformationPolicy {

    /**
     * Json to json transformation configuration
     */
    private final JsonToJsonTransformationPolicyConfiguration jsonToJsonTransformationPolicyConfiguration;

    public JsonToJsonTransformationPolicy(final JsonToJsonTransformationPolicyConfiguration jsonToJsonTransformationPolicyConfiguration) {
        this.jsonToJsonTransformationPolicyConfiguration = jsonToJsonTransformationPolicyConfiguration;
    }

    @OnResponseContent
    public ReadWriteStream onResponseContent(Response response, ExecutionContext executionContext) {
        if (jsonToJsonTransformationPolicyConfiguration.getScope() == PolicyScope.RESPONSE) {
            TransformableStreamBuilder builder = TransformableResponseStreamBuilder.on(response).transform(map(executionContext));

            if (jsonToJsonTransformationPolicyConfiguration.isOverrideContentType()) {
                builder.contentType(MediaType.APPLICATION_JSON);
            }

            return builder.build();
        }

        return null;
    }

    @OnRequestContent
    public ReadWriteStream onRequestContent(Request request, ExecutionContext executionContext) {
        if (jsonToJsonTransformationPolicyConfiguration.getScope() == PolicyScope.REQUEST) {
            TransformableStreamBuilder builder = TransformableRequestStreamBuilder.on(request).transform(map(executionContext));

            if (jsonToJsonTransformationPolicyConfiguration.isOverrideContentType()) {
                builder.contentType(MediaType.APPLICATION_JSON);
            }

            return builder.build();
        }

        return null;
    }

    Function<Buffer, Buffer> map(ExecutionContext executionContext) {
        return input -> {
            try {
                // Get JOLT specification and transform it using internal template engine
                String specification = executionContext
                    .getTemplateEngine()
                    .convert(jsonToJsonTransformationPolicyConfiguration.getSpecification());

                List<Object> chainrSpecJSON = JsonUtils.jsonToList(specification);
                Chainr chainr = Chainr.fromSpec(chainrSpecJSON);

                Object inputJSON = JsonUtils.jsonToObject(input.toString());
                Object transformedOutput = chainr.transform(inputJSON);

                return Buffer.buffer(JsonUtils.toJsonString(transformedOutput));
            } catch (Exception ex) {
                throw new TransformationException("Unable to apply JSON to JSON transformation: " + ex.getMessage(), ex);
            }
        };
    }
}
