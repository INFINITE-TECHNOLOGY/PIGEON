package io.infinite.tpn.http

import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel

abstract class SenderAbstract {

    HttpRequest httpRequest
    HttpResponse httpResponse

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    SenderAbstract(HttpRequest httpRequest) {
        this.httpRequest = httpRequest
    }

    abstract void sendHttpMessage()

}
