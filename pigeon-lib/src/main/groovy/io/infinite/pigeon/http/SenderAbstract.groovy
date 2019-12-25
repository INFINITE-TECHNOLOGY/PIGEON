package io.infinite.pigeon.http

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.supplies.ast.exceptions.ExceptionUtils

@BlackBox
@ToString(includeNames = true, includeFields = true)
@Slf4j
abstract class SenderAbstract {

    abstract void sendHttpMessage(HttpRequest httpRequest, HttpResponse httpResponse)

    void fail(HttpRequest httpRequest, Exception connectException, MessageStatuses messageStatus) {
        httpRequest.exceptionString = new ExceptionUtils().stacktrace(connectException)
        log.warn("Exception during sending:")
        log.warn(httpRequest.exceptionString)
        httpRequest.requestStatus = messageStatus.value()
    }

}
