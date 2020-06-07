package io.infinite.pigeon.threads

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.pigeon.config.OutputQueue
import io.infinite.pigeon.entities.OutputMessage
import io.infinite.pigeon.other.MessageStatusSets
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import java.time.Duration
import java.time.Instant

@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
@ToString(includeNames = true, includeFields = true, includeSuper = true)
@Component
@Scope("prototype")
class OutputThreadRetry extends OutputThread {

    OutputThreadRetry(OutputQueue outputQueue) {
        super(outputQueue)
        senderThreadRobin.clear()
        name = name + "_RETRY"
        (1..outputQueue.retryThreadCount).each { threadCounter ->
            SenderThread senderThread = applicationContext.getBean(SenderThread.class, outputQueue, "_RETRY_" + threadCounter)
            senderThreadRobin.add(senderThread)
            senderThread.start()
        }
    }

    @BlackBox(level = CarburetorLevel.ERROR)
    LinkedHashSet<OutputMessage> masterQuery(String outputQueueName) {
        Date maxLastSendDate = (Instant.now() - Duration.ofSeconds(outputQueue.resendIntervalSeconds)).toDate()
        return outputMessageRepository.masterQueryRetry(outputQueueName, MessageStatusSets.OUTPUT_RETRY_MESSAGE_STATUSES.value(), outputQueue.maxRetryCount, maxLastSendDate)
    }

    @BlackBox(level = CarburetorLevel.ERROR, suppressExceptions = true)
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
            mainCycle()
            sleep(outputQueue.pollPeriodMillisecondsRetry)
        }
    }

}
