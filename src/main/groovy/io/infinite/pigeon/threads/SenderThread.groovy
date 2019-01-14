package io.infinite.pigeon.threads


import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.pigeon.conf.OutputQueue
import io.infinite.pigeon.http.HttpRequest
import io.infinite.pigeon.http.SenderAbstract
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.pigeon.other.PigeonException
import io.infinite.pigeon.springdatarest.HttpLog
import io.infinite.pigeon.springdatarest.InputMessageRepository
import io.infinite.pigeon.springdatarest.OutputMessage
import io.infinite.pigeon.springdatarest.OutputMessageRepository
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource

import java.util.concurrent.LinkedBlockingQueue

@Slf4j
@BlackBox
class SenderThread extends Thread {

    @Autowired
    InputMessageRepository inputMessageRepository

    @Autowired
    OutputMessageRepository outputMessageRepository

    OutputThread outputThread

    LinkedBlockingQueue<OutputMessage> sendingQueue = new LinkedBlockingQueue<>()

    GroovyScriptEngine groovyScriptEngine

    SenderThread(OutputThread outputThread, Integer id, String pigeonOutPluginsDir) {
        setName(outputThread.getName() + "_SENDER_" + id)
        this.outputThread = outputThread
        this.groovyScriptEngine = new GroovyScriptEngine(pigeonOutPluginsDir, this.getClass().getClassLoader())
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
        httpRequest.httpProperties = outputQueue.httpProperties
        httpRequest.extensions = outputQueue.extensions
        return httpRequest
    }

    @BlackBox(level = CarburetorLevel.EXPRESSION, suppressExceptions = true)
    void sendMessage(OutputMessage outputMessage) {
        try {
            Binding binding = new Binding()
            HttpRequest httpRequest = createHttpRequest(outputThread.outputQueue)
            binding.setVariable("outputMessage", outputMessage)
            binding.setVariable("outputQueue", outputThread.outputQueue)
            binding.setVariable("inputMessage", outputMessage.getInputMessage())
            binding.setVariable("httpRequest", httpRequest)
            /*\/\/\/\/\/\/\/\/*/
            groovyScriptEngine.run(outputThread.outputQueue.getConversionModuleName(), binding)//<<<<<<<<<<<<<conversion happens here
            /*/\/\/\/\/\/\/\/\*/
            SenderAbstract senderAbstract = Class.forName(outputThread.outputQueue.getSenderClassName()).newInstance(httpRequest) as SenderAbstract
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
        } catch (Exception e) {
            outputMessage.setExceptionString(new PigeonException(e).serialize())
            outputMessage.setStatus(MessageStatuses.EXCEPTION.value())
            outputMessageRepository.save(outputMessage)
            throw e
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

    @Override
    String toString() {
        return "Thread: " + getName() + "; OutputQueue: " + outputThread.outputQueue.toString() + "; sendingQueue: " + sendingQueue.toString()
    }

}
