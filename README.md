# Infinite Technology ∞ Pigeon 🐦

**HTTP Message Broker.**

> Post pigeons have been (and sometimes still are) extensively used for quick delivery of paper-based messages (such as text, drawings, maps) - thus the project name.

It is capable to:
1) Enqueue a textual message from external source
2) Convert it into one or more HTTP messages with a specified body/query string parameters using appropriate Plugins
3) Dispatch the resulting messages ansynchronously towards one or more recipients (URLs) using a variety of HTTP connection and authentication mechanisms (such as AWS v4 signature)
4) If needed retry sending the message several times

References:
* [**Pigeon Documentation**](https://github.com/INFINITE-TECHNOLOGY/PIGEON/wiki)

Release info:

|Attribute\Release type|Latest|Stable|
|----------------------|------|------|
|Version|1.0.0-SNAPSHOT|N/A|
|Branch|[master](https://github.com/INFINITE-TECHNOLOGY/PIGEON)|N/A|
|CI Build status|[![Build Status](https://travis-ci.com/INFINITE-TECHNOLOGY/PIGEON.svg?branch=master)](https://travis-ci.com/INFINITE-TECHNOLOGY/PIGEON)|N/A|
|Test coverage|[![codecov](https://codecov.io/gh/INFINITE-TECHNOLOGY/PIGEON/branch/master/graphs/badge.svg)](https://codecov.io/gh/INFINITE-TECHNOLOGY/PIGEON/branch/master/graphs)|N/A|
|Application distributive|[Download](https://github.com/INFINITE-TECHNOLOGY/PIGEON/releases/download/1.0.0-SNAPSHOT/pigeon-1.0.0-SNAPSHOT.jar)|N/A|
|Library (Maven)|[oss.jfrog.org snapshot](https://oss.jfrog.org/artifactory/webapp/#/artifacts/browse/tree/General/oss-snapshot-local/io/infinite/pigeon-lib/1.0.0-SNAPSHOT)|N/A|

Technical details:
* Spring Boot
* Groovy
* Functionality extensible using Plugins (Groovy scripts)
* Scalable (multithreaded app with configurable outbound thread pool sizes for load balancing and scalability)

Sample configuration:

```json
{
  "inputQueues": [
    {
      "name": "SMSGLOBAL",
      "outputQueues": [
        {
          "name": "SMSGLOBAL",
          "url": "https://api.smsglobal.com/http-api.php",
          "maxRetryCount": 0,
          "normalThreadCount": 4,
          "retryThreadCount": 0,
          "conversionModuleName": "PASSTHROUGH_GET.groovy",
          "senderClassName": "io.infinite.pigeon.http.SenderDefaultHttps",
          "httpProperties": {
            "username": "smsglobaluser",
            "password": "smsglobalpassword"
          },
          "extensions": {
            "from": "your%20service"
          }
        },
        {
          "name": "SMSGLOBAL_CLOSED_LOOP",
          "pollPeriodMilliseconds": 500,
          "url": "http://localhost:8089/pigeon/plugins/input/http/MOCK_SMSGLOBAL_HTTP",
          "maxRetryCount": 0,
          "normalThreadCount": 4,
          "retryThreadCount": 0,
          "conversionModuleName": "PASSTHROUGH_GET.groovy",
          "senderClassName": "io.infinite.pigeon.http.SenderDefaultHttp",
          "httpProperties": {
            "username": "smsglobaluser",
            "password": "smsglobalpassword"
          },
          "extensions": {
            "from": "your%service"
          }
        }
      ]
    }
  ]
}
```
