package io.infinite.tpn.http

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel

import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext
import java.security.SecureRandom

@Slf4j
class SenderDefaultHttpsUnsecure extends SenderDefault {

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    SenderDefaultHttpsUnsecure(HttpRequest httpRequest) {
        super(httpRequest)
        SSLContext sslContext = SSLContext.getInstance("TLS")
        UnsecureTrustManager[] unsecureTrustManagers = new UnsecureTrustManager()
        sslContext.init(null as KeyManager[], unsecureTrustManagers, null as SecureRandom)
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory())
        httpURLConnection = (HttpsURLConnection) url.openConnection()
        httpURLConnection.setHostnameVerifier(new UnsecureHostNameVerifier())
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    void sendHttpMessage() {
        log.warn("UNSECURE TEST TLS MODE IS USED")
        log.warn("DO NOT USE ON PRODUCTION")
        super.sendHttpMessage()
    }

}