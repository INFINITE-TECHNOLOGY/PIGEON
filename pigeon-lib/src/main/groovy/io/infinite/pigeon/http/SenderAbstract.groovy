package io.infinite.pigeon.http

import groovy.transform.ToString
import io.infinite.blackbox.BlackBox

@BlackBox
@ToString(includeNames = true, includeFields = true)
abstract class SenderAbstract {

    abstract void sendHttpMessage(HttpRequest httpRequest, HttpResponse httpResponse)

}
