# [2.1.0](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/2.0.1...2.1.0) (2023-05-29)


### Features

* provide execution phase in manifest ([a1302ac](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/a1302ac4aac1bb77060d3f77d87581bddb9cc3f3))

## [2.0.1](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/2.0.0...2.0.1) (2023-04-06)


### Bug Fixes

* Provide message to the TemplateEngine for message transformation ([12bf61c](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/12bf61c7e9b092ceecc70851e0ded3b6fd574693))

# [2.0.0](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/1.7.1...2.0.0) (2023-03-17)


### Bug Fixes

* **deps:** upgrade gravitee-bom & alpha version ([f8ad9f2](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/f8ad9f26eba5a2b321bb063dc5d0297588615ef3))
* rename 'jupiter' package in 'reactive' ([6bae723](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/6bae723074feab0a0238a265b166c83602c4bd7d))


### Features

* apply json transformation on messages ([aaef745](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/aaef745b5a5bc1c01cbaf2c8dd34239b1e8b28c2))
* ignore request/response having non json body ([f2fbf8f](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/f2fbf8f34dbc0a5fe61b678f130a553d9ca84b62))


### BREAKING CHANGES

* Requires APIM 3.20 minimum because it requires RxJava3.

For request/response transformation in V4 engine, the policy will
apply only when a JSON like the Content-Type header is defined.

https://gravitee.atlassian.net/browse/APIM-40

# [2.0.0-alpha.2](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/2.0.0-alpha.1...2.0.0-alpha.2) (2023-03-13)


### Bug Fixes

* rename 'jupiter' package in 'reactive' ([6bae723](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/6bae723074feab0a0238a265b166c83602c4bd7d))

# [2.0.0-alpha.1](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/1.7.1...2.0.0-alpha.1) (2023-01-04)


### Features

* apply json transformation on messages ([aaef745](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/aaef745b5a5bc1c01cbaf2c8dd34239b1e8b28c2))
* ignore request/response having non json body ([f2fbf8f](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/f2fbf8f34dbc0a5fe61b678f130a553d9ca84b62))


### BREAKING CHANGES

* Requires APIM 3.20 minimum because it requires RxJava3.

For request/response transformation in V4 engine, the policy will
apply only when a JSON like the Content-Type header is defined.

https://gravitee.atlassian.net/browse/APIM-40

## [1.7.1](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/1.7.0...1.7.1) (2022-04-28)


### Bug Fixes

* use chain for TransformableStream to fail if TransformationException ([8c7cc3a](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/8c7cc3a866ac5575c0079371efd8e9b4da71a423))

## [1.6.1](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/1.6.0...1.6.1) (2022-03-04)


### Bug Fixes

* use chain for TransformableStream to fail if TransformationException ([8c7cc3a](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/8c7cc3a866ac5575c0079371efd8e9b4da71a423))


# [1.7.0](https://github.com/gravitee-io/gravitee-policy-json-to-json/compare/1.6.0...1.7.0) (2022-01-24)


### Features

* bump gravitee-common and gravitee-gateway-api ([864a351](https://github.com/gravitee-io/gravitee-policy-json-to-json/commit/864a351d1fdabc85c99e407e6134d5a1c33bec98)), closes [gravitee-io/issues#6937](https://github.com/gravitee-io/issues/issues/6937)
