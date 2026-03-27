
<!-- GENERATED CODE - DO NOT ALTER THIS OR THE FOLLOWING LINES -->
# JSON to JSON Transformation

[![Gravitee.io](https://img.shields.io/static/v1?label=Available%20at&message=Gravitee.io&color=1EC9D2)](https://download.gravitee.io/#graviteeio-apim/plugins/policies/gravitee-policy-json-to-json/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/gravitee-io/gravitee-policy-json-to-json/blob/master/LICENSE.txt)
[![Releases](https://img.shields.io/badge/semantic--release-conventional%20commits-e10079?logo=semantic-release)](https://github.com/gravitee-io/gravitee-policy-json-to-json/releases)
[![CircleCI](https://circleci.com/gh/gravitee-io/gravitee-policy-json-to-json.svg?style=svg)](https://circleci.com/gh/gravitee-io/gravitee-policy-json-to-json)

## Overview
# JSON to JSON transformation policy

[![Available at Gravitee.io](https://github.com/gravitee-io/gravitee-policy-json-to-json)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/gravitee-io/gravitee-policy-webhook-signature-generator/blob/master/LICENSE.txt)
[![Releases](https://img.shields.io/badge/semantic--release-conventional%20commits-e10079?logo=semantic-release)](https://github.com/gravitee-io/gravitee-policy-webhook-signature-generator/releases)
[![CircleCI](https://circleci.com/gh/gravitee-io/webhook-logo.svg?style=svg)](https://circleci.com/gh/gravitee-io/gravitee-policy-webhook-signature-generator)
# Phases

### V3 engine

| onRequestContent | onResponseContent |
|:----------------:|:-----------------:|
|        X         |         X         |

### V4 engine

| onRequest | onResponse | onMessageRequest | onMessageResponse | onMessageRequest (Native Kafka) | onMessageResponse (Native Kafka) |
|:---------:|:----------:|:----------------:|:-----------------:|:-------------------------------:|:--------------------------------:|
|     X     |     X      |        X         |         X         |                X                |                X                 |

## Description

You can use the `json-to-json` policy to apply a transformation (or mapping) on the request and/or response and/or
message content.

This policy is based on the https://github.com/bazaarvoice/jolt[JOLT^] library.

In APIM, you need to provide the JOLT specification in the policy configuration.

NOTE: You can use APIM EL in the JOLT specification.

At request/response level, the policy will do nothing if the processed request/response does not contain JSON. This
policy checks the `Content-Type` header before applying any transformation.

At message level, the policy will do nothing if the processed message has no content. It means that the message will be
re-emitted as is.

# Compatibility with APIM

| Plugin version | APIM version  |
|:--------------:|:-------------:|
|      1.X       | Up to 3.19.x  |
|      2.X       |    3.20.x     |
|      3.X       | 4.x to latest |

# Configuration

You can configure the policy with the following options:

|      Property       |      Required      | Description                                                                                         | Type   |  Default  |
|:-------------------:|:------------------:|-----------------------------------------------------------------------------------------------------|--------|:---------:|
|        scope        | only for v3 engine | The execution scope (`request` or `response`)                                                       | string | `REQUEST` |
|    specification    |         X          | The http://jolt-demo.appspot.com/[JOLT^] specification to apply on a given content.Can contains EL. | string |           |
| overrideContentType |                    | Override the Content-Type to `application/json`                                                     | string |  `true`   |
|                     |                    |                                                                                                     |        |           |

### Example configuration

```json
{
"json-to-json": 
  {
    "scope": "REQUEST",
    "specification": "[{ \"operation\": \"shift\", \"spec\": { \"_id\": \"id\", \"*\": { \"$\": \"&1\" } } }, { \"operation\": \"remove\", \"spec\": { \"__v\": \"\" } }]"
  }
}
```


## Usage
### For this input:

```json
{
    "_id": "57762dc6ab7d620000000001",
    "name": "name",
    "__v": 0
}
```

### And this JOLT specification:

```json
[
    {
        "operation": "shift",
        "spec": 
        {
            "_id": "id",
            "*": 
            {
              "$": "&1"
            } 
        }
    },
    {
        "operation": "remove",
        "spec": 
        {
          "__v": ""
        }
    }
]
```

The output is as follows:

```json
{
    "id": "57762dc6ab7d620000000001",
    "name": "name"
}
```



## Errors
These templates are defined at the API level, in the "Entrypoint" section for v4 APIs, or in "Response Templates" for v2 APIs.
The error keys sent by this policy are as follows:

| Key |
| ---  |
| INVALID_JSON_TRANSFORMATION |



## Phases
The `json-to-json` policy can be applied to the following API types and flow phases.

### Compatible API types

* `PROXY`
* `MESSAGE`
* `NATIVE KAFKA`

### Supported flow phases:

* Publish
* Subscribe
* Request
* Response

## Compatibility matrix
Strikethrough text indicates that a version is deprecated.

| Plugin version| APIM |
| --- | ---  |
|3.0.x|4.1.x and above |
|4.0.x|4.10.9 and above |


## Configuration options


#### 
| Name <br>`json name`  | Type <br>`constraint`  | Mandatory  | Default  | Description  |
|:----------------------|:-----------------------|:----------:|:---------|:-------------|
| Override the Content-Type<br>`overrideContentType`| boolean|  | `true`| Enforce the Content-Type: application/json|
| JOLT specification<br>`specification`| string| ✅| | |




## Examples

*Proxy API on Request phase*
```json
{
  "api": {
    "definitionVersion": "V4",
    "type": "PROXY",
    "name": "JSON to JSON Transformation example API",
    "flows": [
      {
        "name": "Common Flow",
        "enabled": true,
        "selectors": [
          {
            "type": "HTTP",
            "path": "/",
            "pathOperator": "STARTS_WITH"
          }
        ],
        "request": [
          {
            "name": "JSON to JSON Transformation",
            "enabled": true,
            "policy": "json-to-json",
            "configuration":
              {
                  "specification": "[{\"operation\":\"shift\",\"spec\":{\"_id\":\"id\",\"*\":{\"$\":\"&1\"}}},{\"operation\":\"remove\",\"spec\":{\"__v\":\"\"}}]"
              }
          }
        ]
      }
    ]
  }
}

```
*Proxy API on Response phase*
```json
{
  "api": {
    "definitionVersion": "V4",
    "type": "PROXY",
    "name": "JSON to JSON Transformation example API",
    "flows": [
      {
        "name": "Common Flow",
        "enabled": true,
        "selectors": [
          {
            "type": "HTTP",
            "path": "/",
            "pathOperator": "STARTS_WITH"
          }
        ],
        "response": [
          {
            "name": "JSON to JSON Transformation",
            "enabled": true,
            "policy": "json-to-json",
            "configuration":
              {
                  "specification": "[{\"operation\":\"shift\",\"spec\":{\"_id\":\"id\",\"*\":{\"$\":\"&1\"}}},{\"operation\":\"remove\",\"spec\":{\"__v\":\"\"}}]"
              }
          }
        ]
      }
    ]
  }
}

```
*Proxy API on Request phase - no override content*
```json
{
  "api": {
    "definitionVersion": "V4",
    "type": "PROXY",
    "name": "JSON to JSON Transformation example API",
    "flows": [
      {
        "name": "Common Flow",
        "enabled": true,
        "selectors": [
          {
            "type": "HTTP",
            "path": "/",
            "pathOperator": "STARTS_WITH"
          }
        ],
        "request": [
          {
            "name": "JSON to JSON Transformation",
            "enabled": true,
            "policy": "json-to-json",
            "configuration":
              {
                  "specification": "[{\"operation\":\"shift\",\"spec\":{\"_id\":\"id\",\"*\":{\"$\":\"&1\"}}},{\"operation\":\"remove\",\"spec\":{\"__v\":\"\"}}]",
                  "overrideContentType": false
              }
          }
        ]
      }
    ]
  }
}

```
*Message API CRD subscribe*
```yaml
apiVersion: "gravitee.io/v1alpha1"
kind: "ApiV4Definition"
metadata:
    name: "json-to-json-message-api-crd"
spec:
    name: "JSON to JSON Transformation example"
    type: "MESSAGE"
    flows:
      - name: "Common Flow"
        enabled: true
        selectors:
            matchRequired: false
            mode: "DEFAULT"
        subscribe:
          - name: "JSON to JSON Transformation"
            enabled: true
            policy: "json-to-json"
            configuration:
              specification: '[{"operation":"shift","spec":{"_id":"id","*":{"$":"&1"}}}]'

```
*Message API CRD - no override content*
```yaml
apiVersion: "gravitee.io/v1alpha1"
kind: "ApiV4Definition"
metadata:
    name: "json-to-json-message-api-crd"
spec:
    name: "JSON to JSON Transformation example"
    type: "MESSAGE"
    flows:
      - name: "Common Flow"
        enabled: true
        selectors:
            matchRequired: false
            mode: "DEFAULT"
        publish:
          - name: "JSON to JSON Transformation"
            enabled: true
            policy: "json-to-json"
            configuration:
              overrideContentType: false
              specification: '[{"operation":"shift","spec":{"_id":"id","*":{"$":"&1"}}}]'

```
*Natvie Kafka API CRD*
```yaml
apiVersion: "gravitee.io/v1alpha1"
kind: "ApiV4Definition"
metadata:
    name: "json-to-json-kafka-native-api-crd"
spec:
    name: "JSON to JSON Transformation example"
    type: "NATIVE"
    flows:
      - name: "Common Flow"
        enabled: true
        selectors:
            matchRequired: false
            mode: "DEFAULT"
        subscribe:
          - name: "JSON to JSON Transformation"
            enabled: true
            policy: "json-to-json"
            configuration:
              specification: '[{"operation":"shift","spec":{"_id":"id","*":{"$":"&1"}}}]'

```
*Message API CRD - no override content*
```yaml
apiVersion: "gravitee.io/v1alpha1"
kind: "ApiV4Definition"
metadata:
    name: "json-to-json-kafka-native-api-crd"
spec:
    name: "JSON to JSON Transformation example"
    type: "NATIVE"
    flows:
      - name: "Common Flow"
        enabled: true
        selectors:
            matchRequired: false
            mode: "DEFAULT"
        publish:
          - name: "JSON to JSON Transformation"
            enabled: true
            policy: "json-to-json"
            configuration:
              overrideContentType: false
              specification: '[{"operation":"shift","spec":{"_id":"id","*":{"$":"&1"}}}]'

```


## Changelog

#### [3.0.1](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/3.0.0...3.0.1) (2023-07-20)


##### Bug Fixes

* update policy description ([9cefc00](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/9cefc00fc11d1f7a0f4ce5ec6fcb6b2a406fa081))

### [3.0.0](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/2.2.0...3.0.0) (2023-07-18)


##### Bug Fixes

* bump gravitee versions ([15ada72](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/15ada72747eefcb3b069b0018d37fd1d21add343))


##### chore

* **deps:** update gravitee-parent ([3e1ee80](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/3e1ee80b8c9c73950a3e86f6eaede12dfa5e79a4))


##### BREAKING CHANGES

* **deps:** require Java17
* Use apim V4 versions

### [3.0.0-alpha.2](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/3.0.0-alpha.1...3.0.0-alpha.2) (2023-07-18)


##### chore

* **deps:** update gravitee-parent ([3e1ee80](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/3e1ee80b8c9c73950a3e86f6eaede12dfa5e79a4))


##### BREAKING CHANGES

* **deps:** require Java17

### [3.0.0-alpha.1](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/2.2.0...3.0.0-alpha.1) (2023-06-29)


##### Bug Fixes

* bump gravitee versions ([15ada72](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/15ada72747eefcb3b069b0018d37fd1d21add343))


##### BREAKING CHANGES

* Use apim V4 versions

### [2.2.0](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/2.1.0...2.2.0) (2023-06-27)


##### Features

* clean json-schema ([eb0f5b4](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/eb0f5b429ae9802eeef50e7585f3facd3fc12289))

### [2.1.0](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/2.0.1...2.1.0) (2023-05-29)


##### Features

* provide execution phase in manifest ([a1302ac](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/a1302ac4aac1bb77060d3f77d87581bddb9cc3f3))

#### [2.0.1](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/2.0.0...2.0.1) (2023-04-06)


##### Bug Fixes

* Provide message to the TemplateEngine for message transformation ([12bf61c](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/12bf61c7e9b092ceecc70851e0ded3b6fd574693))

### [2.0.0](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/1.7.1...2.0.0) (2023-03-17)


##### Bug Fixes

* **deps:** upgrade gravitee-bom & alpha version ([f8ad9f2](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/f8ad9f26eba5a2b321bb063dc5d0297588615ef3))
* rename 'jupiter' package in 'reactive' ([6bae723](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/6bae723074feab0a0238a265b166c83602c4bd7d))


##### Features

* apply json transformation on messages ([aaef745](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/aaef745b5a5bc1c01cbaf2c8dd34239b1e8b28c2))
* ignore request/response having non json body ([f2fbf8f](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/f2fbf8f34dbc0a5fe61b678f130a553d9ca84b62))


##### BREAKING CHANGES

* Requires APIM 3.20 minimum because it requires RxJava3.

For request/response transformation in V4 engine, the policy will
apply only when a JSON like the Content-Type header is defined.

https://gravitee.atlassian.net/browse/APIM-40

### [2.0.0-alpha.2](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/2.0.0-alpha.1...2.0.0-alpha.2) (2023-03-13)


##### Bug Fixes

* rename 'jupiter' package in 'reactive' ([6bae723](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/6bae723074feab0a0238a265b166c83602c4bd7d))

### [2.0.0-alpha.1](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/1.7.1...2.0.0-alpha.1) (2023-01-04)


##### Features

* apply json transformation on messages ([aaef745](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/aaef745b5a5bc1c01cbaf2c8dd34239b1e8b28c2))
* ignore request/response having non json body ([f2fbf8f](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/f2fbf8f34dbc0a5fe61b678f130a553d9ca84b62))


##### BREAKING CHANGES

* Requires APIM 3.20 minimum because it requires RxJava3.

For request/response transformation in V4 engine, the policy will
apply only when a JSON like the Content-Type header is defined.

https://gravitee.atlassian.net/browse/APIM-40

#### [1.7.1](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/1.7.0...1.7.1) (2022-04-28)


##### Bug Fixes

* use chain for TransformableStream to fail if TransformationException ([8c7cc3a](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/8c7cc3a866ac5575c0079371efd8e9b4da71a423))

#### [1.6.1](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/1.6.0...1.6.1) (2022-03-04)


##### Bug Fixes

* use chain for TransformableStream to fail if TransformationException ([8c7cc3a](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/8c7cc3a866ac5575c0079371efd8e9b4da71a423))


### [1.7.0](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/1.6.0...1.7.0) (2022-01-24)


##### Features

* bump gravitee-common and gravitee-gateway-api ([864a351](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/864a351d1fdabc85c99e407e6134d5a1c33bec98)), closes [gravitee-io/issues#6937](https://github.com/gravitee-io/issues/issues/6937)

