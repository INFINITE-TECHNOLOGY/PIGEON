package io.infinite.tpn.http

import groovy.transform.ToString
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel

import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory

@BlackBox
@ToString(includeNames = true, includeFields = true, includeSuper = true)
class SenderDefaultHttps extends SenderDefault {

    SenderDefaultHttps(HttpRequest httpRequest) {
        super(httpRequest)
        HttpsURLConnection.setDefaultSSLSocketFactory(SSLSocketFactory.getDefault() as SSLSocketFactory)
        httpURLConnection = (HttpsURLConnection) url.openConnection()
    }

}