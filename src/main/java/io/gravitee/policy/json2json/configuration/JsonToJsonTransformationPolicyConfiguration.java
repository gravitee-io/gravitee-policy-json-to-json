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
package io.gravitee.policy.json2json.configuration;

import io.gravitee.policy.api.PolicyConfiguration;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class JsonToJsonTransformationPolicyConfiguration implements PolicyConfiguration {

    private PolicyScope scope = PolicyScope.REQUEST;

    private String specification;

    //by default, we override the content-type to be backward compatible
    private boolean overrideContentType = true;

    public PolicyScope getScope() {
        return scope;
    }

    public void setScope(PolicyScope scope) {
        this.scope = scope;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public boolean isOverrideContentType() {
        return overrideContentType;
    }

    public void setOverrideContentType(boolean overrideContentType) {
        this.overrideContentType = overrideContentType;
    }
}
