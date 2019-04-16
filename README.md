# Infinite Technology ∞ Pigeon 🕊

|Attribute\Release type|Latest|
|----------------------|------|
|Version|1.0.0-SNAPSHOT|
|Branch|[master](https://github.com/INFINITE-TECHNOLOGY/PIGEON)|
|CI Build status|[![Build Status](https://travis-ci.com/INFINITE-TECHNOLOGY/PIGEON.svg?branch=master)](https://travis-ci.com/INFINITE-TECHNOLOGY/PIGEON)|
|Test coverage|[![codecov](https://codecov.io/gh/INFINITE-TECHNOLOGY/PIGEON/branch/master/graphs/badge.svg)](https://codecov.io/gh/INFINITE-TECHNOLOGY/PIGEON/branch/master/graphs)|
|Library (Maven)|[oss.jfrog.org snapshot](https://oss.jfrog.org/artifactory/webapp/#/artifacts/browse/tree/General/oss-snapshot-local/io/infinite/pigeon/1.0.0-SNAPSHOT)|
|Heroku|![Heroku](https://heroku-badge.herokuapp.com/?app=pigeon-public&root=/pigeon/inputMessages)|

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

## Try me now!

We have deployed a demo [Pigeon Plugins](https://github.com/INFINITE-TECHNOLOGY/PIGEON_PLUGINS) repository is as a demo Heroku app (`pigeon-public`).

Just open the below URL in your browser:

https://pigeon-public.herokuapp.com/pigeon/enqueue?source=browser&endpoint=GET_TO_SMTP&recipient=email@gmail.com&subject=Test123&text=Test1234&from=pigeon@i-t.io

This demo Heroku `pigeon-public` app asynchronously enqueues and sends a email.

* Replace `email@gmail.com` with your email (we will not save/share/store/disclose it, it is fully private)<br/>
* Only Gmail addresses are supported in this demo<br/>
* You can change also subject, text and from <br/>
* Check `spam` folder in your Gmail account<br/>
* Navigate through returned URLs to see message status and HTTP logs<br/>
* First time request may take up to 50 seconds, due to free Heroku dyno unidlying startup.

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
