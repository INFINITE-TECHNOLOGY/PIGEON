package io.infinite.pigeon.threads


import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.http.HttpRequest
import io.infinite.http.HttpResponse
import io.infinite.http.SenderAbstract
import io.infinite.pigeon.config.OutputQueue
import io.infinite.pigeon.entities.HttpLog
import io.infinite.pigeon.entities.OutputMessage
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.pigeon.repositories.HttpLogRepository
import io.infinite.pigeon.repositories.OutputMessageRepository
import io.infinite.supplies.ast.exceptions.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired

import java.util.concurrent.LinkedBlockingQueue

@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
@ToString(includeNames = true, includeFields = true, includeSuper = true)
class SenderThread extends Thread {

    @Autowired
    HttpLogRepository httpLogRepository

    @Autowired
    OutputMessageRepository outputMessageRepository

    OutputThread outputThread

    LinkedBlockingQueue<OutputMessage> sendingQueue = new LinkedBlockingQueue<>()

    GroovyScriptEngine groovyScriptEngine

    SenderThread(OutputThread outputThread, Integer id, String pigeonOutPluginsDir) {
        super(new ThreadGroup("SENDER"), outputThread.name + "_SENDER_" + id)
        this.outputThread = outputThread
        this.groovyScriptEngine = new GroovyScriptEngine(pigeonOutPluginsDir, this.class.classLoader)
    }

    @Override
    void run() {
        while (true) {
            while (!sendingQueue.isEmpty()) {
                try {
                    sendMessage(sendingQueue.poll())
                } catch (Exception e) {
                    println("Sender thread exception.")
                    println(new ExceptionUtils().stacktrace(e))
                    log.error("Sender thread exception.", e)
                }
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

    void sendMessage(OutputMessage outputMessage) {
        try {
            Binding binding = new Binding()
            HttpRequest httpRequest = createHttpRequest(outputThread.outputQueue)
            binding.setVariable("outputMessage", outputMessage)
            binding.setVariable("outputQueue", outputThread.outputQueue)
            binding.setVariable("inputMessage", outputMessage.inputMessage)
            binding.setVariable("httpRequest", httpRequest)
            /*\/\/\/\/\/\/\/\/*///<<<<conversion happens here
            try {
                groovyScriptEngine.run(outputThread.outputQueue.conversionModuleName, binding)
            } catch (Exception e) {
                log.warn("Output plugin exception (Output Message ${outputMessage.id})")
                outputMessage.exceptionString = new ExceptionUtils().stacktrace(e)
                outputMessage.status = MessageStatuses.EXCEPTION.value()
                outputMessageRepository.saveAndFlush(outputMessage)
                return
            }
            /*/\/\/\/\/\/\/\/\*/
            SenderAbstract senderAbstract = Class.forName(outputThread.outputQueue.senderClassName).newInstance() as SenderAbstract
            outputMessage.status = MessageStatuses.SENDING.value()
            outputMessage.attemptsCount = outputMessage.attemptsCount + 1
            outputMessage = outputMessageRepository.saveAndFlush(outputMessage)
            /*\/\/\/\/\/\/\/\/*/
            HttpResponse httpResponse = new HttpResponse()
            try {
                httpResponse = senderAbstract.sendHttpMessage(httpRequest)//<<<<<<<<<<<sending message
            } finally {
                outputMessage.httpLogs.add(createHttpLog(httpRequest, httpResponse, outputMessage))
            }
            /*/\/\/\/\/\/\/\/\*/
            outputMessage.status = httpRequest.requestStatus
            outputMessage.exceptionString = httpRequest.exceptionString
            outputMessage.lastSendTime = new Date()
        } catch (Exception e) {
            outputMessage.status = MessageStatuses.EXCEPTION.value()
            outputMessage.exceptionString = new ExceptionUtils().stacktrace(e)
            log.warn("Sending exception (Output Message ${outputMessage.id})")
        } finally {
            outputMessageRepository.saveAndFlush(outputMessage)
        }
    }

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
        httpLog.senderThreadName = name
        return httpLog
    }

    @Override
    String toString() {
        return "Thread: " + name + "; OutputQueue: " + outputThread.outputQueue.toString() + "; messages: " + sendingQueue.toString()
    }

}
