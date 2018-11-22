package io.infinite.tpn.threads

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.tpn.conf.OutputQueue
import io.infinite.tpn.http.HttpRequest
import io.infinite.tpn.http.SenderAbstract
import io.infinite.tpn.other.MessageStatuses
import io.infinite.tpn.other.TpnException
import io.infinite.tpn.springdatarest.HttpLog
import io.infinite.tpn.springdatarest.InputMessageRepository
import io.infinite.tpn.springdatarest.OutputMessage
import io.infinite.tpn.springdatarest.OutputMessageRepository
import org.springframework.beans.factory.annotation.Autowired

import java.util.concurrent.LinkedBlockingQueue

@Slf4j
@BlackBox
class SenderThread extends Thread {

    @Autowired
    InputMessageRepository inputMessageRepository

    @Autowired
    OutputMessageRepository outputMessageRepository

    OutputQueue outputQueue

    LinkedBlockingQueue<OutputMessage> sendingQueue = new LinkedBlockingQueue<>()

    GroovyScriptEngine groovyScriptEngine = new GroovyScriptEngine("./conf/conversion_modules/")

    SenderThread(OutputQueue outputQueue, Integer id) {
        setName("Sender_" + outputQueue.getName() + "_" + id)
        this.outputQueue = outputQueue
    }

    @Override
    void run() {
        while (true) {
            while (!sendingQueue.isEmpty()) {
                sendMessage(sendingQueue.poll())
            }
            synchronized (this) {
                this.wait()
            }
        }
    }

    static HttpRequest createHttpRequest(OutputQueue outputQueue) {
        HttpRequest httpRequest = new HttpRequest()
        httpRequest.url = outputQueue.url
        httpRequest.awsServiceName = outputQueue.awsServiceName
        httpRequest.awsRegion = outputQueue.awsRegion
        httpRequest.awsAccessKey = outputQueue.awsAccessKey
        httpRequest.awsSecretKey = outputQueue.awsSecretKey
        httpRequest.awsResourceName = outputQueue.awsResourceName
        return httpRequest
    }

    void sendMessage(OutputMessage outputMessage) {
        try {
            Binding binding = new Binding()
            HttpRequest httpRequest = createHttpRequest(outputQueue)
            binding.setVariable("outputMessage", outputMessage)
            binding.setVariable("outputQueue", outputQueue)
            binding.setVariable("inputMessage", outputMessage.getInputMessage())
            binding.setVariable("httpRequest", httpRequest)
            /*\/\/\/\/\/\/\/\/*/
            groovyScriptEngine.run(outputQueue.getConversionModuleName(), binding)//<<<<<<<<<<<<<conversion happens here
            /*/\/\/\/\/\/\/\/\*/
            SenderAbstract senderAbstract = Class.forName(outputQueue.getSenderClassName()).newInstance(httpRequest) as SenderAbstract
            outputMessage.setStatus(MessageStatuses.SENDING.value())
            outputMessageRepository.save(outputMessage)
            /*\/\/\/\/\/\/\/\/*/
            senderAbstract.sendHttpMessage()//<<<<<<<<<<<sending message
            /*/\/\/\/\/\/\/\/\*/
            outputMessage.getHttpLogs().add(createHttpLog(senderAbstract))
            outputMessage.setStatus(httpRequest.getRequestStatus())
            outputMessage.setAttemptsCount(outputMessage.getAttemptsCount() + 1)
            outputMessage.setLastSendTime(new Date())
            outputMessageRepository.save(outputMessage)
        } catch (Throwable t) {
            outputMessage.setExceptionString(new TpnException(t).serialize())
            outputMessage.setStatus(MessageStatuses.EXCEPTION.value())
            outputMessageRepository.save(outputMessage)
            throw t
        }
    }

    static HttpLog createHttpLog(SenderAbstract senderAbstract) {
        HttpLog httpLog = new HttpLog()
        httpLog.requestDate = senderAbstract.httpRequest.sendDate
        httpLog.requestHeaders = senderAbstract.httpRequest.headers.toString()
        httpLog.requestBody = senderAbstract.httpRequest.body
        httpLog.method = senderAbstract.httpRequest.method
        httpLog.url = senderAbstract.httpRequest.url
        httpLog.requestStatus = senderAbstract.httpRequest.requestStatus
        httpLog.requestExceptionString = senderAbstract.httpRequest.exceptionString
        httpLog.responseDate = senderAbstract.httpResponse.receiveDate
        httpLog.responseHeaders = senderAbstract.httpResponse.headers.toString()
        httpLog.responseBody = senderAbstract.httpResponse.body
        httpLog.responseStatus = senderAbstract.httpResponse.status
        return httpLog
    }
}
