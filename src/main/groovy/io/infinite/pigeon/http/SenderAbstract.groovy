package io.infinite.pigeon.http

import groovy.transform.ToString
import io.infinite.blackbox.BlackBox

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
