package io.infinite.pigeon.http

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.supplies.ast.exceptions.ExceptionUtils

import java.nio.charset.StandardCharsets

import static java.net.HttpURLConnection.HTTP_CREATED
import static java.net.HttpURLConnection.HTTP_OK

@BlackBox
@ToString(includeNames = true, includeFields = true, includeSuper = true)
@Slf4j
abstract class SenderDefault extends SenderAbstract {

    Integer DEFAULT_CONNECT_TIMEOUT = 15000
    Integer DEFAULT_READ_TIMEOUT = 15000

    URLConnection openConnection(HttpRequest httpRequest) {
        URL url = new URL(httpRequest.url)
        URLConnection urlConnection
        if (httpRequest.httpProperties?.get("proxyHost") != null && httpRequest.httpProperties?.get("proxyPort") != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpRequest.httpProperties.get("proxyHost") as String, httpRequest.httpProperties.get("proxyPort") as Integer))
            urlConnection = url.openConnection(proxy)
        } else {
            urlConnection = url.openConnection()
        }
        if (httpRequest.httpProperties?.get("connectTimeout") != null && httpRequest.httpProperties?.get("connectTimeout") == true) {
            urlConnection.connectTimeout = httpRequest.httpProperties?.get("connectTimeout") as int
        } else {
            urlConnection.connectTimeout = DEFAULT_CONNECT_TIMEOUT
        }
        if (httpRequest.httpProperties?.get("readTimeout") != null && httpRequest.httpProperties?.get("readTimeout") == true) {
            urlConnection.readTimeout = httpRequest.httpProperties?.get("readTimeout") as int
        } else {
            urlConnection.readTimeout = DEFAULT_READ_TIMEOUT
        }
        if (httpRequest.httpProperties?.get("basicAuthEnabled") != null && httpRequest.httpProperties?.get("basicAuthEnabled") == true) {
            String username = httpRequest.httpProperties?.get("username")
            String password = httpRequest.httpProperties?.get("password")
            if (username != null && username != "" && password != null && password != "") {
                String encoded = Base64.encoder.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8))
                httpRequest.headers.put("Authorization", "Basic " + encoded)
                //urlConnection.setRequestProperty("Authorization", "Basic " + encoded)
            } else {
                log.warn("Basic authentication is enabled but username and password are not defined")
            }
        }
        return urlConnection
    }

    void sendHttpMessageWithUrlConnection(HttpRequest httpRequest, HttpResponse httpResponse, HttpURLConnection httpURLConnection) {
        try {
            httpURLConnection.requestMethod = httpRequest.method
            for (headerName in httpRequest.headers.keySet()) {
                httpURLConnection.setRequestProperty(headerName, httpRequest.headers.get(headerName))
            }
            if (httpRequest.method == "POST") {
                httpURLConnection.doOutput = true
            }
            try {
                httpURLConnection.connect()
            } catch (ConnectException connectException) { //no timeout set
                fail(httpRequest, connectException, MessageStatuses.FAILED_NO_CONNECTION)
                return
            } catch (SocketTimeoutException connectException) {
                //in this situation ONLY connection timeout; NO read timeout.
                fail(httpRequest, connectException, MessageStatuses.FAILED_NO_CONNECTION)
                return
            }
            if (httpRequest.method == "POST") {
                DataOutputStream dataOutputStream
                dataOutputStream = new DataOutputStream(httpURLConnection.outputStream)
                if (httpRequest.body != null) {
                    dataOutputStream.writeBytes(httpRequest.body)
                } else {
                    log.warn("POST request with empty body")
                }
                dataOutputStream.flush()
                dataOutputStream.close()
            }
            log.trace("Successfully sent request data:")
            log.trace(httpRequest.toString())
            Integer responseCode
            responseCode = httpURLConnection.responseCode
            httpResponse.status = responseCode
            InputStream inputStream = getInputStream(httpURLConnection)
            if (inputStream != null) {
                httpResponse.body = inputStream.text
            } else {
                log.trace("Null input stream")
            }
            for (headerName in httpURLConnection.headerFields.keySet()) {
                httpResponse.getHeaders().put(headerName, httpURLConnection.getHeaderField(headerName))
            }
            if ([HTTP_OK, HTTP_CREATED].contains(httpResponse.status)) {
                httpRequest.requestStatus = MessageStatuses.DELIVERED.value()
            } else {
                log.warn("Failed response status: " + httpResponse.status)
                httpRequest.requestStatus = MessageStatuses.FAILED_RESPONSE.value()
            }
        } catch (Exception e) {
            fail(httpRequest, e, MessageStatuses.EXCEPTION)
        } finally {
            log.trace("Received response data:")
            log.trace(httpResponse.toString())
            try {
                httpURLConnection.disconnect()
                closeInputStream(httpURLConnection)
            } catch (Exception disconnectException) {
                log.warn("Exception during releasing connection:")
                log.warn(new ExceptionUtils().stacktrace(disconnectException))
            }
        }
    }

    InputStream getInputStream(HttpURLConnection httpURLConnection) {
        InputStream inputStream = null
        if (httpURLConnection.errorStream == null) {
            if (httpURLConnection.responseCode == HTTP_OK) {
                inputStream = httpURLConnection.inputStream
            }
        } else {
            inputStream = httpURLConnection.errorStream
        }
        return inputStream
    }

    void closeInputStream(HttpURLConnection httpURLConnection) {
        if (httpURLConnection.errorStream == null) {
            if (httpURLConnection.responseCode == HTTP_OK) {
                httpURLConnection.inputStream.close()
            }
        } else {
            httpURLConnection.errorStream.close()
        }
    }

}