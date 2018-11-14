package io.infinite.tpn.http

import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.tpn.other.TpnException

class SenderDefaultHttp extends SenderDefault {

    SenderDefaultHttp(HttpRequest httpRequest) {
        super(httpRequest)
        if (url.getProtocol().contains("https")) {
            throw new TpnException("Invalid protocol \"https\" for SenderDefaultHttp in ${httpRequest.url}. Use \"http\" protocol.")
        }
        httpURLConnection = (HttpURLConnection) url.openConnection()
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    void sendHttpMessage() {
        log.warn("UNSECURE TEST PLAINTEXT HTTP CONNECTION")
        log.warn("DO NOT USE ON PRODUCTION")
        super.sendHttpMessage()
    }

}