package io.infinite.pigeon.threads


import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.pigeon.config.OutputQueue
import io.infinite.pigeon.entities.OutputMessage
import io.infinite.pigeon.other.MessageStatusSets
import io.infinite.supplies.ast.exceptions.ExceptionUtils

import java.time.Duration
import java.time.Instant

@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
@ToString(includeNames = true, includeFields = true, includeSuper = true)
class OutputThreadRetry extends OutputThread {

    OutputThreadRetry(OutputQueue outputQueue, InputThread inputThread) {
        super(outputQueue, inputThread)
        senderThreadRobin.clear()
        name = name + "_RETRY"
        (1..outputQueue.retryThreadCount).each {
            SenderThread senderThread = new SenderThread(this, it)
            senderThread.name = senderThread.name + "_RETRY"
            applicationContext.autowireCapableBeanFactory.autowireBean(senderThread)
            senderThreadRobin.add(senderThread)
            senderThread.start()
        }
    }

    @BlackBox(level = CarburetorLevel.ERROR)
    LinkedHashSet<OutputMessage> masterQuery(String outputQueueName) {
        Date maxLastSendDate = (Instant.now() - Duration.ofSeconds(outputQueue.resendIntervalSeconds)).toDate()
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
