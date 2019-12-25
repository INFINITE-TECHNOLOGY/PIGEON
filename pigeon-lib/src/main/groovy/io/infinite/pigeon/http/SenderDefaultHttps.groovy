package io.infinite.pigeon.http

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory

@BlackBox
@ToString(includeNames = true, includeFields = true, includeSuper = true)
@Slf4j
class SenderDefaultHttps extends SenderDefault {

    @Override
    void sendHttpMessage(HttpRequest httpRequest, HttpResponse httpResponse) {
        HttpsURLConnection.defaultSSLSocketFactory = SSLSocketFactory.default as SSLSocketFactory
        HttpURLConnection httpURLConnection = (HttpsURLConnection) openConnection(httpRequest)
        super.sendHttpMessageWithUrlConnection(httpRequest, httpResponse, httpURLConnection)
    }

}