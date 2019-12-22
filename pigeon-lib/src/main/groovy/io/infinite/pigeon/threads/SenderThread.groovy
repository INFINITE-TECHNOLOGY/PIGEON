package io.infinite.pigeon.threads

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.conf.OutputQueue
import io.infinite.pigeon.http.HttpRequest
import io.infinite.pigeon.http.HttpResponse
import io.infinite.pigeon.http.SenderAbstract
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.pigeon.springdatarest.entities.HttpLog
import io.infinite.pigeon.springdatarest.entities.OutputMessage
import io.infinite.pigeon.springdatarest.repositories.HttpLogRepository
import io.infinite.pigeon.springdatarest.repositories.OutputMessageRepository
import io.infinite.supplies.ast.exceptions.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

import java.util.concurrent.LinkedBlockingQueue

@BlackBox
@Slf4j
class SenderThread extends Thread {

    @Autowired
    HttpLogRepository httpLogRepository

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
    @Transactional
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
            SenderAbstract senderAbstract = Class.forName(outputThread.outputQueue.getSenderClassName()).newInstance() as SenderAbstract
            outputMessage.setStatus(MessageStatuses.SENDING.value())
            outputMessage.setAttemptsCount(outputMessage.attemptsCount + 1)
            outputMessageRepository.save(outputMessage)
            /*\/\/\/\/\/\/\/\/*/
            HttpResponse httpResponse = new HttpResponse()
            try {
                senderAbstract.sendHttpMessage(httpRequest, httpResponse)//<<<<<<<<<<<sending message
            } finally {
                outputMessage.getHttpLogs().add(createHttpLog(httpRequest, httpResponse, outputMessage))
            }
            /*/\/\/\/\/\/\/\/\*/
            outputMessage.setStatus(httpRequest.requestStatus)
            outputMessage.setExceptionString(httpRequest.exceptionString)
            outputMessage.setLastSendTime(new Date())
        } catch (Exception e) {
            outputMessage.setStatus(MessageStatuses.EXCEPTION.value())
            outputMessage.setExceptionString(new ExceptionUtils().stacktrace(e))
            log.warn("Sending exception (Output Message ${outputMessage.id})")
        } finally {
            outputMessageRepository.save(outputMessage)
        }
    }

    @Transactional
    HttpLog createHttpLog(HttpRequest httpRequest, HttpResponse httpResponse, OutputMessage outputMessage) {
        //todo: null status/exception
        HttpLog httpLog = new HttpLog()
        httpLog.requestDate = httpRequest?.sendDate
        httpLog.requestHeaders = httpRequest?.headers?.toString()
        httpLog.requestBody = httpRequest?.body
        httpLog.method = httpRequest?.method
        httpLog.url = httpRequest?.url
        httpLog.requestStatus = httpRequest?.requestStatus
        httpLog.requestExceptionString = httpRequest?.exceptionString
        httpLog.responseDate = httpResponse?.receiveDate
        httpLog.responseHeaders = httpResponse?.headers?.toString()
        httpLog.responseBody = httpResponse?.body
        httpLog.responseStatus = httpResponse?.status
        httpLog.outputMessage = outputMessage
        httpLog.senderThreadName = getName()
        httpLogRepository.save(httpLog)
        return httpLog
    }

    @Override
    String toString() {
        return "Thread: " + getName() + "; OutputQueue: " + outputThread.outputQueue.toString() + "; messages: " + sendingQueue.toString()
    }

}
