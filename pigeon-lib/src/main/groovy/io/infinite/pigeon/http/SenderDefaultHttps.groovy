package io.infinite.pigeon.http

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory

@BlackBox
@ToString(includeNames = true, includeFields = true, includeSuper = true)
@CompileStatic
class SenderDefaultHttps extends SenderDefault {

    private final transient Logger log = LoggerFactory.getLogger(this.getClass().getCanonicalName())

    SenderDefaultHttps(HttpRequest httpRequest) {
        super(httpRequest)
        HttpsURLConnection.setDefaultSSLSocketFactory(SSLSocketFactory.getDefault() as SSLSocketFactory)
        httpURLConnection = (HttpsURLConnection) openConnection()
    }

}