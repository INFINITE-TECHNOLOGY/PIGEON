package io.infinite.tpn.http

import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel

@BlackBox
abstract class SenderAbstract {

    HttpRequest httpRequest
    HttpResponse httpResponse

    SenderAbstract(HttpRequest httpRequest) {
        this.httpRequest = httpRequest
        this.httpResponse = new HttpResponse()
    }

    abstract void sendHttpMessage()

}
