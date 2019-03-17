# Infinite Technology ∞ Pigeon 🕊

|Attribute\Release type|Latest|Stable|
|----------------------|------|------|
|Version|1.0.0-SNAPSHOT|1.0.x|
|Branch|[master](https://github.com/INFINITE-TECHNOLOGY/PIGEON)|[PIGEON_1_0_X](https://github.com/INFINITE-TECHNOLOGY/PIGEON/tree/PIGEON_1_0_X)|
|CI Build status|[![Build Status](https://travis-ci.com/INFINITE-TECHNOLOGY/PIGEON.svg?branch=master)](https://travis-ci.com/INFINITE-TECHNOLOGY/PIGEON)|[![Build Status](https://travis-ci.com/INFINITE-TECHNOLOGY/PIGEON.svg?branch=PIGEON_1_0_X)](https://travis-ci.com/INFINITE-TECHNOLOGY/PIGEON)|
|Test coverage|[![codecov](https://codecov.io/gh/INFINITE-TECHNOLOGY/PIGEON/branch/master/graphs/badge.svg)](https://codecov.io/gh/INFINITE-TECHNOLOGY/PIGEON/branch/master/graphs)|[![codecov](https://codecov.io/gh/INFINITE-TECHNOLOGY/PIGEON/branch/PIGEON_1_0_X/graphs/badge.svg)](https://codecov.io/gh/INFINITE-TECHNOLOGY/PIGEON/branch/PIGEON_1_0_X/graphs)|
|Library (Maven)|[oss.jfrog.org snapshot](https://oss.jfrog.org/artifactory/webapp/#/artifacts/browse/tree/General/oss-snapshot-local/io/infinite/pigeon/1.0.0-SNAPSHOT)|[ ![Download](https://api.bintray.com/packages/infinite-technology/m2/pigeon/images/download.svg) ](https://bintray.com/infinite-technology/m2/pigeon/_latestVersion)|

## Purpose

`Pigeon` is an end-user server application (HTTP Message Broker) designed for distribution of text messages in HTTP format.


## In short

`Pigeon` is capable to:
1) Enqueue a text message from external source using REST API or direct insert into Pigeon DB by the external app
2) Convert it into one or more HTTP messages with a specified body/query string parameters using appropriate `Plugins` (plugins can be developed by end-users using Groovy script)
3) Dispatch the resulting messages ansynchronously to one or more recipients (URLs) using a variety of HTTP connection and authentication mechanisms (such as AWS v4 signature)
4) If needed retry sending the message several times

## Documentation

* [**Pigeon Documentation**](https://github.com/INFINITE-TECHNOLOGY/PIGEON/wiki)

## Technology stack

* Spring Boot
* Groovy
* SQL DB (via JPA and Spring Data)
* REST+HATEOAS (via Spring Data Rest repositories)
* Functionality extensible using Plugins (Groovy scripts)
* Scalable (multithreaded app with configurable outbound thread pool sizes for load balancing and scalability)

## Sample configuration

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
