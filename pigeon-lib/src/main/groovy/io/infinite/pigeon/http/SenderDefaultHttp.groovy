package io.infinite.pigeon.http

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.other.PigeonException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@BlackBox
@ToString(includeNames = true, includeFields = true, includeSuper = true)
class SenderDefaultHttp extends SenderDefault {

    private final transient Logger log = LoggerFactory.getLogger(this.getClass().getCanonicalName())

    SenderDefaultHttp(HttpRequest httpRequest) {
        super(httpRequest)
        httpURLConnection = (HttpURLConnection) openConnection()
    }

    @Override
    void sendHttpMessage() {
        log.warn("UNSECURE TEST PLAINTEXT HTTP CONNECTION")
        log.warn("DO NOT USE ON PRODUCTION")
        if (url.getProtocol().contains("https")) {
            throw new PigeonException("Invalid protocol 'https' for SenderDefaultHttp in ${httpRequest.url}. Use 'http' protocol.")
        }
        super.sendHttpMessage()
    }

}