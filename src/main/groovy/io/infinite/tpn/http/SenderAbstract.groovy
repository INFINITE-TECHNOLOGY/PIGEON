package io.infinite.tpn.http

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel

@Slf4j
abstract class SenderAbstract {

    HttpRequest httpRequest
    HttpResponse httpResponse

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    SenderAbstract(HttpRequest httpRequest) {
        this.httpRequest = httpRequest
    }

    abstract void sendHttpMessage()

}
