package io.infinite.pigeon.threads

import groovy.time.TimeCategory
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.pigeon.conf.OutputQueue
import io.infinite.pigeon.other.MessageStatusSets
import io.infinite.pigeon.springdatarest.entities.OutputMessage
import io.infinite.supplies.ast.exceptions.ExceptionUtils
import org.springframework.context.ApplicationContext

@BlackBox
@Slf4j
class OutputThreadRetry extends OutputThread {

    OutputThreadRetry(OutputQueue outputQueue, InputThread inputThread, ApplicationContext applicationContext) {
        super(outputQueue, inputThread, applicationContext)
        senderThreadRobin.clear()
        name = name + "_RETRY"
        (1..outputQueue.retryThreadCount).each {
            SenderThread senderThread = new SenderThread(this, it, applicationContext.environment.getProperty("pigeonOutPluginsDir"))
            senderThread.name = senderThread.name + "_RETRY"
            applicationContext.autowireCapableBeanFactory.autowireBean(senderThread)
            senderThreadRobin.add(senderThread)
            senderThread.start()
        }
    }

    @BlackBox(level = CarburetorLevel.ERROR)
    @CompileDynamic
    LinkedHashSet<OutputMessage> masterQuery(String outputQueueName) {
        Date maxLastSendDate
        use(TimeCategory) {
            maxLastSendDate = (new Date() - outputQueue.resendIntervalSeconds.seconds)
        }
        return outputMessageRepository.masterQueryRetry(outputQueueName, MessageStatusSets.OUTPUT_RETRY_MESSAGE_STATUSES.value(), outputQueue.maxRetryCount, maxLastSendDate)
    }

    @BlackBox(level = CarburetorLevel.ERROR)
    void mainCycle() {
        Set<OutputMessage> outputMessages = masterQuery(outputQueue.name)
        if (outputMessages.size() > 0) {
            outputMessages.each { outputMessage ->
                senderEnqueue(outputMessage)
            }
        }
    }

    @Override
    @BlackBox(level = CarburetorLevel.METHOD)
    void run() {
        while (true) {
            try {
                mainCycle()
                sleep(outputQueue.pollPeriodMillisecondsRetry)
            } catch (Exception e) {
                println("Output retry thread exception.")
                println(new ExceptionUtils().stacktrace(e))
                log.error("Output retry thread exception.", e)
            }
        }
    }

}
