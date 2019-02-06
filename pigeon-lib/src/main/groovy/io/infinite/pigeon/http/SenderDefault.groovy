package io.infinite.pigeon.http

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.supplies.ast.exceptions.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets

import static java.net.HttpURLConnection.HTTP_CREATED
import static java.net.HttpURLConnection.HTTP_OK

@BlackBox
@ToString(includeNames = true, includeFields = true, includeSuper = true)
abstract class SenderDefault extends SenderAbstract {

    private final transient Logger log = LoggerFactory.getLogger(this.getClass().getCanonicalName())

    URL url
    HttpURLConnection httpURLConnection

    SenderDefault(HttpRequest httpRequest) {
        super(httpRequest)
        this.url = new URL(httpRequest.getUrl())
    }

    URLConnection openConnection() {
        URLConnection urlConnection
        if (httpRequest.httpProperties?.get("proxyHost") != null && httpRequest.httpProperties?.get("proxyPort") != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpRequest.httpProperties.get("proxyHost") as String, httpRequest.httpProperties.get("proxyPort") as Integer))
            urlConnection = url.openConnection(proxy)
        } else {
            urlConnection = url.openConnection()
        }
        if (httpRequest.httpProperties?.get("basicAuthEnabled") != null && httpRequest.httpProperties?.get("basicAuthEnabled") == true) {
            String username = httpRequest.httpProperties?.get("username")
            String password = httpRequest.httpProperties?.get("password")
            if (username != null && username != "" && password != null && password != "") {
                String encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8))
                httpRequest.headers.put("Authorization", "Basic " + encoded)
                //urlConnection.setRequestProperty("Authorization", "Basic " + encoded)
            } else {
                log.warn("Basic authentication is enabled but username and password are not defined")
            }
        }
        return urlConnection
    }

    @Override
    void sendHttpMessage() {
        this.httpURLConnection.setRequestMethod(httpRequest.getMethod())
        for (headerName in httpRequest.getHeaders().keySet()) {
            httpURLConnection.setRequestProperty(headerName, httpRequest.getHeaders().get(headerName))
        }
        httpURLConnection.setDoOutput(true)
        DataOutputStream dataOutputStream
        try {
            dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream())
        } catch (ConnectException connectException) {
            httpRequest.setExceptionString(new ExceptionUtils().stacktrace(connectException))
            log.warn("Exception during connection:")
            log.warn(httpRequest.getExceptionString())
            httpRequest.setRequestStatus(MessageStatuses.FAILED_NO_CONNECTION.value())
            return
        }
        if (httpRequest.getBody() != null) {
            dataOutputStream.writeBytes(httpRequest.getBody())
        }
        dataOutputStream.flush()
        dataOutputStream.close()
        log.info("Successfully sent request data:")
        log.info(httpRequest.toString())
        httpResponse.setStatus(httpURLConnection.getResponseCode())
        InputStream inputStream = getInputStream(httpURLConnection)
        if (inputStream != null) {
            httpResponse.setBody(inputStream.getText())
        } else {
            log.info("Null input stream")
        }
        for (headerName in httpURLConnection.getHeaderFields().keySet()) {
            httpResponse.getHeaders().put(headerName, httpURLConnection.getHeaderField(headerName))
        }
        if ([HTTP_OK, HTTP_CREATED].contains(httpResponse.getStatus())) {
            httpRequest.setRequestStatus(MessageStatuses.DELIVERED.value())
        } else {
            log.warn("Failed response status: " + httpResponse.getStatus())
            httpRequest.setRequestStatus(MessageStatuses.FAILED_RESPONSE.value())
        }
        log.info("Received response data:")
        log.info(httpResponse.toString())
    }

    InputStream getInputStream(HttpURLConnection httpURLConnection) {
        InputStream inputStream = null
        if (httpURLConnection.getErrorStream() == null) {
            if (httpURLConnection.getResponseCode() == HTTP_OK) {
                inputStream = httpURLConnection.getInputStream()
            }
        } else {
            inputStream = httpURLConnection.getErrorStream()
        }
        return inputStream
    }

}