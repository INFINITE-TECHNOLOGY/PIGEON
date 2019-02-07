package io.infinite.pigeon.http

import groovy.transform.CompileStatic
import groovy.transform.ToString
import io.infinite.blackbox.BlackBox

import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

@ToString(includeNames = true, includeFields = true)
@BlackBox
@CompileStatic
class UnsecureTrustManager implements X509TrustManager {

    @Override
    X509Certificate[] getAcceptedIssuers() {
        return null as X509Certificate[]
    }

    @Override
    void checkClientTrusted(X509Certificate[] certs, String authType) {
    }

    @Override
    void checkServerTrusted(X509Certificate[] certs, String authType) {
    }

}
