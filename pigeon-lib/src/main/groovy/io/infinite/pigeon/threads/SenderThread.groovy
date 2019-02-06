package io.infinite.pigeon.threads

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.conf.OutputQueue
import io.infinite.pigeon.http.HttpRequest
import io.infinite.pigeon.http.SenderAbstract
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.pigeon.springdatarest.HttpLog
import io.infinite.pigeon.springdatarest.InputMessageRepository
import io.infinite.pigeon.springdatarest.OutputMessage
import io.infinite.pigeon.springdatarest.OutputMessageRepository
import io.infinite.supplies.ast.exceptions.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import java.util.concurrent.LinkedBlockingQueue

@BlackBox
class SenderThread extends Thread {

    private final transient Logger log = LoggerFactory.getLogger(this.getClass().getCanonicalName())

    @Autowired
    InputMessageRepository inputMessageRepository

    @Autowired
    OutputMessageRepository outputMessageRepository

    OutputThread outputThread

    LinkedBlockingQueue<OutputMessage> sendingQueue = new LinkedBlockingQueue<>()

    GroovyScriptEngine groovyScriptEngine

    SenderThread(OutputThread outputThread, Integer id, String pigeonOutPluginsDir) {
        super(new ThreadGroup("SENDER"), outputThread.getName() + "_SENDER_" + id)
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

    HttpRequest createHttpRequest(OutputQueue outputQueue) {
        HttpRequest httpRequest = new HttpRequest()
        httpRequest.url = outputQueue.url
        httpRequest.httpProperties = outputQueue.httpProperties
        httpRequest.extensions = outputQueue.extensions
        return httpRequest
    }

    @BlackBox
    void sendMessage(OutputMessage outputMessage) {
        try {
            Binding binding = new Binding()
            HttpRequest httpRequest = createHttpRequest(outputThread.outputQueue)
            binding.setVariable("outputMessage", outputMessage)
            binding.setVariable("outputQueue", outputThread.outputQueue)
            binding.setVariable("inputMessage", outputMessage.getInputMessage())
            binding.setVariable("httpRequest", httpRequest)
            /*\/\/\/\/\/\/\/\/*///<<<<conversion happens here
            try {
                groovyScriptEngine.run(outputThread.outputQueue.getConversionModuleName(), binding)
            } catch (Exception e) {
                log.warn("Output plugin exception (Output Message ${outputMessage.id})")
                outputMessage.setExceptionString(new ExceptionUtils().stacktrace(e))
                outputMessage.setStatus(MessageStatuses.EXCEPTION.value())
                outputMessageRepository.save(outputMessage)
                return
            }
            /*/\/\/\/\/\/\/\/\*/
            SenderAbstract senderAbstract = Class.forName(outputThread.outputQueue.getSenderClassName()).newInstance(httpRequest) as SenderAbstract
            outputMessage.setStatus(MessageStatuses.SENDING.value())
            outputMessageRepository.save(outputMessage)
            /*\/\/\/\/\/\/\/\/*/
            try {
                senderAbstract.sendHttpMessage()//<<<<<<<<<<<sending message
            } finally {
                outputMessage.getHttpLogs().add(createHttpLog(senderAbstract, outputMessage))
            }
            /*/\/\/\/\/\/\/\/\*/
            outputMessage.setStatus(httpRequest.getRequestStatus())
            outputMessage.setAttemptsCount(outputMessage.getAttemptsCount() + 1)
            outputMessage.setLastSendTime(new Date())
        } catch (Exception e) {
            outputMessage.setExceptionString(new ExceptionUtils().stacktrace(e))
            outputMessage.setStatus(MessageStatuses.EXCEPTION.value())
            log.warn("Sending exception (Output Message ${outputMessage.id})")
        } finally {
            outputMessageRepository.save(outputMessage)
        }
    }

    HttpLog createHttpLog(SenderAbstract senderAbstract, OutputMessage outputMessage) {
        HttpLog httpLog = new HttpLog()
        httpLog.requestDate = senderAbstract.httpRequest?.sendDate
        httpLog.requestHeaders = senderAbstract.httpRequest?.headers?.toString()
        httpLog.requestBody = senderAbstract.httpRequest?.body
        httpLog.method = senderAbstract.httpRequest?.method
        httpLog.url = senderAbstract.httpRequest?.url
        httpLog.requestStatus = senderAbstract.httpRequest?.requestStatus
        httpLog.requestExceptionString = senderAbstract.httpRequest?.exceptionString
        httpLog.responseDate = senderAbstract.httpResponse?.receiveDate
        httpLog.responseHeaders = senderAbstract.httpResponse?.headers?.toString()
        httpLog.responseBody = senderAbstract.httpResponse?.body
        httpLog.responseStatus = senderAbstract.httpResponse?.status
        httpLog.outputMessage = outputMessage
        return httpLog
    }

    @Override
    String toString() {
        return "Thread: " + getName() + "; OutputQueue: " + outputThread.outputQueue.toString() + "; messages: " + sendingQueue.toString()
    }

}
