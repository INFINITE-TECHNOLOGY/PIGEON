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
        URL url = new URL(httpRequest.getUrl())
        URLConnection urlConnection
        if (httpRequest.httpProperties?.get("proxyHost") != null && httpRequest.httpProperties?.get("proxyPort") != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpRequest.httpProperties.get("proxyHost") as String, httpRequest.httpProperties.get("proxyPort") as Integer))
            urlConnection = url.openConnection(proxy)
        } else {
            urlConnection = url.openConnection()
        }
        if (httpRequest.httpProperties?.get("connectTimeout") != null && httpRequest.httpProperties?.get("connectTimeout") == true) {
            urlConnection.setConnectTimeout(httpRequest.httpProperties?.get("connectTimeout") as int)
        } else {
            urlConnection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
        }
        if (httpRequest.httpProperties?.get("readTimeout") != null && httpRequest.httpProperties?.get("readTimeout") == true) {
            urlConnection.setReadTimeout(httpRequest.httpProperties?.get("readTimeout") as int)
        } else {
            urlConnection.setReadTimeout(DEFAULT_READ_TIMEOUT)
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

    void sendHttpMessageWithUrlConnection(HttpRequest httpRequest, HttpResponse httpResponse, HttpURLConnection httpURLConnection) {
        try {
            httpURLConnection.setRequestMethod(httpRequest.method)
            for (headerName in httpRequest.getHeaders().keySet()) {
                httpURLConnection.setRequestProperty(headerName, httpRequest.getHeaders().get(headerName))
            }
            if (httpRequest.method == "POST") {
                httpURLConnection.setDoOutput(true)
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
                dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream())
                if (httpRequest.getBody() != null) {
                    dataOutputStream.writeBytes(httpRequest.getBody())
                } else {
                    log.warn("POST request with empty body")
                }
                dataOutputStream.flush()
                dataOutputStream.close()
            }
            log.info("Successfully sent request data:")
            log.info(httpRequest.toString())
            Integer responseCode
            responseCode = httpURLConnection.getResponseCode()
            httpResponse.setStatus(responseCode)
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
        } catch (Exception e) {
            fail(httpRequest, e, MessageStatuses.EXCEPTION)
        } finally {
            log.info("Received response data:")
            log.info(httpResponse.toString())
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
        if (httpURLConnection.getErrorStream() == null) {
            if (httpURLConnection.getResponseCode() == HTTP_OK) {
                inputStream = httpURLConnection.getInputStream()
            }
        } else {
            inputStream = httpURLConnection.getErrorStream()
        }
        return inputStream
    }

    void closeInputStream(HttpURLConnection httpURLConnection) {
        if (httpURLConnection.getErrorStream() == null) {
            if (httpURLConnection.getResponseCode() == HTTP_OK) {
                httpURLConnection.getInputStream().close()
            }
        } else {
            httpURLConnection.getErrorStream().close()
        }
    }

}