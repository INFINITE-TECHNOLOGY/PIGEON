package io.infinite.tpn.http

import groovy.util.logging.Slf4j

@Slf4j
abstract class SenderAbstract {

    HttpRequest httpRequest
    HttpResponse httpResponse

    SenderAbstract(HttpRequest httpRequest) {
        this.httpRequest = httpRequest
    }

    abstract void sendHttpMessage()

}
