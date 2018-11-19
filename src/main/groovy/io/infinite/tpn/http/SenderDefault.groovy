package io.infinite.tpn.http

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.blackbox.Wait
import io.infinite.tpn.other.MessageStatuses
import org.apache.commons.lang3.exception.ExceptionUtils
import org.codehaus.groovy.runtime.StackTraceUtils

@Slf4j
abstract class SenderDefault extends SenderAbstract {

    URL url
    HttpURLConnection httpURLConnection

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    SenderDefault(HttpRequest httpRequest) {
        super(httpRequest)
        this.url = new URL(httpRequest.getUrl())
        Wait
    }

    @Override
    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
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
        httpRequest.setSendDate(new Date())
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
            log.warn("Null input stream")
        }
        for (headerName in httpURLConnection.getHeaderFields().keySet()) {
            httpResponse.getHeaders().put(headerName, httpURLConnection.getHeaderField(headerName))
        }
        httpResponse.setReceiveDate(new Date())
        httpRequest.setRequestStatus(MessageStatuses.DELIVERED.value())
        log.info("Received response data:")
        log.info(httpResponse.toString())
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
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