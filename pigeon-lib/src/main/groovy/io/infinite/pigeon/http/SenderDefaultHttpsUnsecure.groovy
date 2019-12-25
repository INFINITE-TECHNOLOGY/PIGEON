package io.infinite.pigeon.http

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext
import java.security.SecureRandom

@BlackBox
@ToString(includeNames = true, includeFields = true, includeSuper = true)
@Slf4j
class SenderDefaultHttpsUnsecure extends SenderDefault {

    @Override
    void sendHttpMessage(HttpRequest httpRequest, HttpResponse httpResponse) {
        SSLContext sslContext = SSLContext.getInstance("TLS")
        UnsecureTrustManager[] unsecureTrustManagers = new UnsecureTrustManager() as UnsecureTrustManager[]
        sslContext.init(null as KeyManager[], unsecureTrustManagers, null as SecureRandom)
        HttpsURLConnection.defaultSSLSocketFactory = sslContext.socketFactory
        HttpURLConnection httpURLConnection = (HttpsURLConnection) openConnection(httpRequest)
        ((HttpsURLConnection) httpURLConnection).hostnameVerifier = new UnsecureHostNameVerifier()
        log.warn("UNSECURE TEST TLS MODE IS USED")
        log.warn("DO NOT USE ON PRODUCTION")
        super.sendHttpMessageWithUrlConnection(httpRequest, httpResponse, httpURLConnection)
    }

}