package io.infinite.tpn.http

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.tpn.other.TpnException

@Slf4j
@BlackBox
class SenderDefaultHttp extends SenderDefault {

    SenderDefaultHttp(HttpRequest httpRequest) {
        super(httpRequest)
        httpURLConnection = (HttpURLConnection) url.openConnection()
    }

    @Override
    void sendHttpMessage() {
        log.warn("UNSECURE TEST PLAINTEXT HTTP CONNECTION")
        log.warn("DO NOT USE ON PRODUCTION")
        if (url.getProtocol().contains("https")) {
            throw new TpnException("Invalid protocol \"https\" for SenderDefaultHttp in ${httpRequest.url}. Use \"http\" protocol.")
        }
        super.sendHttpMessage()
    }

}