package io.infinite.tpn.http

import groovy.transform.ToString
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel

import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

@ToString(includeNames = true, includeFields = true)
class UnsecureTrustManager implements X509TrustManager {

    @Override
    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    X509Certificate[] getAcceptedIssuers() {
        return null as X509Certificate[]
    }

    @Override
    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    void checkClientTrusted(X509Certificate[] certs, String authType) {
    }

    @Override
    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    void checkServerTrusted(X509Certificate[] certs, String authType) {
    }

}
