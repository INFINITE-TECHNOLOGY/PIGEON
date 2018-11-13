package io.infinite.tpn.http

import groovy.transform.ToString
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

@ToString(includeNames = true, includeFields = true)
class UnsecureHostNameVerifier implements HostnameVerifier {

    @Override
    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    boolean verify(String hostName, SSLSession sslSession) {
        return true
    }
}
