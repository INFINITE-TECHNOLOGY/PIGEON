package io.infinite.tpn.http

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.tpn.other.MessageStatuses
import org.apache.commons.lang3.exception.ExceptionUtils
import org.codehaus.groovy.runtime.StackTraceUtils
import static java.net.HttpURLConnection.*

@Slf4j
@BlackBox
@ToString(includeNames = true, includeFields = true, includeSuper = true)
abstract class SenderDefault extends SenderAbstract {

    URL url
    HttpURLConnection httpURLConnection

    SenderDefault(HttpRequest httpRequest) {
        super(httpRequest)
        this.url = new URL(httpRequest.getUrl())
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
            httpRequest.setExceptionString(ExceptionUtils.getStackTrace(new StackTraceUtils().sanitizeRootCause(connectException)))
            log.warn("Exception during connection:")
            log.warn(httpRequest.getExceptionString())
            httpRequest.setRequestStatus(MessageStatuses.FAILED_NO_CONNECTION.value())
            return
        }
        dataOutputStream.writeBytes(httpRequest.getBody())
        dataOutputStream.flush()
        dataOutputStream.close()
        log.info("Successfully sent request data:")
        log.info(httpRequest.toString())
        httpResponse.setStatus(httpURLConnection.getResponseCode())
        InputStream inputStream = getInputStream(httpURLConnection)
        if (inputStream != null) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))
            String line
            StringBuffer stringBuffer = new StringBuffer()
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line)
            }
            bufferedReader.close()
            httpResponse.setBody(stringBuffer.toString())
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

    static InputStream getInputStream(HttpURLConnection httpURLConnection) {
        InputStream inputStream = null
        if (httpURLConnection.getErrorStream() == null) {
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = httpURLConnection.getInputStream()
            }
        } else {
            inputStream = httpURLConnection.getErrorStream()
        }
        return inputStream
    }

}