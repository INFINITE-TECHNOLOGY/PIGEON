package io.infinite.pigeon.http

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext
import java.security.SecureRandom

@BlackBox
@ToString(includeNames = true, includeFields = true, includeSuper = true)
class SenderDefaultHttpsUnsecure extends SenderDefault {

    private final transient Logger log = LoggerFactory.getLogger(this.getClass().getCanonicalName())

    SenderDefaultHttpsUnsecure(HttpRequest httpRequest) {
        super(httpRequest)
        SSLContext sslContext = SSLContext.getInstance("TLS")
        UnsecureTrustManager[] unsecureTrustManagers = new UnsecureTrustManager()
        sslContext.init(null as KeyManager[], unsecureTrustManagers, null as SecureRandom)
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory())
        httpURLConnection = (HttpsURLConnection) openConnection()
        httpURLConnection.setHostnameVerifier(new UnsecureHostNameVerifier())
    }

    @Override
    void sendHttpMessage() {
        log.warn("UNSECURE TEST TLS MODE IS USED")
        log.warn("DO NOT USE ON PRODUCTION")
        super.sendHttpMessage()
    }

}