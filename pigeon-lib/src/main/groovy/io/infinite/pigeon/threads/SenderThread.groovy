package io.infinite.pigeon.threads

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.http.HttpRequest
import io.infinite.http.HttpResponse
import io.infinite.http.SenderAbstract
import io.infinite.pigeon.conf.OutputQueue
import io.infinite.pigeon.springdatarest.entities.HttpLog
import io.infinite.pigeon.springdatarest.entities.OutputMessage
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.pigeon.springdatarest.repositories.HttpLogRepository
import io.infinite.pigeon.springdatarest.repositories.OutputMessageRepository
import io.infinite.supplies.ast.exceptions.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import java.util.concurrent.LinkedBlockingQueue

@BlackBox(level = BlackBoxLevel.METHOD)
@Slf4j
@ToString(includeNames = true, includeFields = true, includeSuper = true)
@Component
@Scope("prototype")
class SenderThread extends Thread {

    @Autowired
    HttpLogRepository httpLogRepository

    @Autowired
    OutputMessageRepository outputMessageRepository

    @Value('${pigeonOutPluginsDir}')
    String pigeonOutPluginsDir

    OutputQueue outputQueue

    LinkedBlockingQueue<OutputMessage> sendingQueue = new LinkedBlockingQueue<>()

    GroovyScriptEngine groovyScriptEngine

    SenderThread(OutputQueue outputQueue, String threadNameSuffix) {
        super(new ThreadGroup("SENDER"), outputQueue.name + "_SENDER" + threadNameSuffix)
        this.outputQueue = outputQueue
    }

    @PostConstruct
    void initGroovyScriptEngine() {
        groovyScriptEngine = new GroovyScriptEngine(pigeonOutPluginsDir, this.class.classLoader)
    }

    @Override
    void run() {
        while (true) {
            sendMessage(sendingQueue.take())
        }
    }

    HttpRequest createHttpRequest(OutputQueue outputQueue) {
        HttpRequest httpRequest = new HttpRequest()
        httpRequest.url = outputQueue.url
        httpRequest.httpProperties = outputQueue.httpProperties
        httpRequest.extensions = outputQueue.extensions
        return httpRequest
    }

    @BlackBox(level = BlackBoxLevel.METHOD, suppressExceptions = true)
    void sendMessage(OutputMessage outputMessage) {
        try {
            Binding binding = new Binding()
            HttpRequest httpRequest = createHttpRequest(outputQueue)
            binding.setVariable("outputMessage", outputMessage)
            binding.setVariable("outputQueue", outputQueue)
            binding.setVariable("inputMessage", outputMessage.inputMessage)
            binding.setVariable("httpRequest", httpRequest)
            /*\/\/\/\/\/\/\/\/*///<<<<conversion happens here
            try {
                groovyScriptEngine.run(outputQueue.conversionModuleName, binding)
            } catch (Exception e) {
                log.warn("Output plugin exception (Output Message ${outputMessage.id})", e)
                outputMessage.exceptionString = new ExceptionUtils().stacktrace(e)
                outputMessage.status = MessageStatuses.EXCEPTION.value()
                outputMessageRepository.saveAndFlush(outputMessage)
                return
            }
            /*/\/\/\/\/\/\/\/\*/
            SenderAbstract senderAbstract = Class.forName(fixOldPackageName(outputQueue.senderClassName)).newInstance() as SenderAbstract
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
            log.warn("Sending exception (Output Message ${outputMessage.id})", e)
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
        return "Thread: " + name + "; OutputQueue: " + outputQueue.toString() + "; messages: " + sendingQueue.toString()
    }

    String fixOldPackageName(String oldPackageName) {
        return oldPackageName.replace("io.infinite.pigeon.http.", "io.infinite.http.")
    }

}
