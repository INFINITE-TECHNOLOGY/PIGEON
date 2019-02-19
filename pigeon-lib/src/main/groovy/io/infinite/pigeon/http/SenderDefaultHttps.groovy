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

    SenderDefaultHttps(HttpRequest httpRequest) {
        super(httpRequest)
        HttpsURLConnection.setDefaultSSLSocketFactory(SSLSocketFactory.getDefault() as SSLSocketFactory)
        httpURLConnection = (HttpsURLConnection) openConnection()
    }

}