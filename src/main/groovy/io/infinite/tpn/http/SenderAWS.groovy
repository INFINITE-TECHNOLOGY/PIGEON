package io.infinite.tpn.http

import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel

class SenderAWS extends SenderAbstract {

    SenderAWS(HttpRequest httpRequest) {
        super(httpRequest)
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    void sendHttpMessage() {
    }

}
