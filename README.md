# DSS Validation Service

[Micronaut](https://micronaut.io/) based service for
validating [JAdES Signatures](https://www.etsi.org/deliver/etsi_ts/119100_119199/11918201/01.01.01_60/ts_11918201v010101p.pdf)
using the [eSignature Building Block Digital Signature Service library](https://github.com/esig/dss/tree/master).

## Installation

### Container

The service is provided as a container at [quay.io](https://quay.io/repository/wi_stefan/dss-validation-service).

It can be started with:

```shell
  docker run -p 8080:8080 quay.io/repository/wi_stefan/dss-validation-service
```

### Configuration

Configurations can be provided with the standard mechanisms of the [Micronaut-Framework](https://micronaut.io/),
e.g. [environment variables or application.yaml file](https://github.com/micronaut-projects/micronaut-core/blob/4.8.x/src/main/docs/guide/config/configurationProperties.adoc).
The following table concentrates on the most important configuration parameters.

| Property                   | Env-Var                    | Description                                                                                       | Default |
|----------------------------|----------------------------|---------------------------------------------------------------------------------------------------|---------|
| `micronaut.server.port`    | `MICRONAUT_SERVER_PORT`    | Server port to be used for the listener endpoint.                                                 | 8080    |
| `endpoints.health.enabled` | `ENDPOINTS_HEALTH_ENABLED` | Enables the health endpoint.                                                                      | `true`  |
| `endpoints.health.enabled` | `ENDPOINTS_HEALTH_ENABLED` | Enables the health endpoint.                                                                      | `true`  |
| `general.basepath`         | `GENERAL_BASEPATH`         | Basepath used for the  endpoint                                                                   | ""      |
| `trust.list`               | `TRUST_LIST`               | List of [tusted-list-configs](src/main/java/com/ficodes/iam/configuration/TrustedListConfig.java) | ""      |
| `trust.stores`             | `TRUST_STORES`             | List of [store-configs](src/main/java/com/ficodes/iam/configuration/StoreConfig.java)             | ""      |
