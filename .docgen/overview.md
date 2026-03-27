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