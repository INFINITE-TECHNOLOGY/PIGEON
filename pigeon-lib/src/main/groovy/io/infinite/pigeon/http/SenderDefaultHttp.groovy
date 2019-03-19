package io.infinite.pigeon.http

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.other.PigeonException

@BlackBox
@ToString(includeNames = true, includeFields = true, includeSuper = true)
@Slf4j
class SenderDefaultHttp extends SenderDefault {

    @Override
    void sendHttpMessage(HttpRequest httpRequest, HttpResponse httpResponse) {
        HttpURLConnection httpURLConnection = (HttpURLConnection) openConnection(httpRequest)
        log.warn("UNSECURE TEST PLAINTEXT HTTP CONNECTION")
        log.warn("DO NOT USE ON PRODUCTION")
        if (httpURLConnection.getURL().getProtocol().contains("https")) {
            throw new PigeonException("Invalid protocol 'https' for SenderDefaultHttp in ${httpRequest.url}. Use 'http' protocol.")
        }
        super.sendHttpMessageWithUrlConnection(httpRequest, httpResponse, httpURLConnection)
    }

}