package io.infinite.pigeon.threads

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.pigeon.config.OutputQueue
import io.infinite.pigeon.entities.OutputMessage
import io.infinite.pigeon.other.MessageStatusSets
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import java.time.Duration
import java.time.Instant

@BlackBox(level = BlackBoxLevel.METHOD)
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

    @BlackBox(level = BlackBoxLevel.ERROR, suppressExceptions = true)
    void dbScanRetry() {
        Set<OutputMessage> outputMessages = outputMessageRepository.takeForRetry(
                outputQueue.name,
                MessageStatusSets.OUTPUT_RETRY_MESSAGE_STATUSES.value(),
                outputQueue.maxRetryCount,
                (Instant.now() - Duration.ofSeconds(outputQueue.resendIntervalSeconds)).toDate()
        ).sort { it.id }
        if (outputMessages.size() > 0) {
            outputMessages.each { outputMessage ->
                senderEnqueue(outputMessage)
            }
        }
    }

    @Override
    @BlackBox(level = BlackBoxLevel.METHOD)
    void run() {
        while (true) {
            dbScanRetry()
            sleep(outputQueue.pollPeriodMillisecondsRetry)
        }
    }

}
