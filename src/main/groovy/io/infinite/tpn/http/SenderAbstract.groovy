package io.infinite.tpn.http

import groovy.transform.ToString
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel

@BlackBox
@ToString(includeNames = true, includeFields = true)
abstract class SenderAbstract {

    HttpRequest httpRequest
    HttpResponse httpResponse

    SenderAbstract(HttpRequest httpRequest) {
        this.httpRequest = httpRequest
        this.httpResponse = new HttpResponse()
    }

    abstract void sendHttpMessage()

}
