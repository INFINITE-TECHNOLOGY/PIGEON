package io.infinite.pigeon.threads

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.pigeon.config.OutputQueue
import io.infinite.pigeon.entities.OutputMessage
import io.infinite.pigeon.other.MessageStatusSets
import io.infinite.pigeon.services.PigeonService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
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
    }

    @PostConstruct
    void initSenderRetryThreads() {
        (1..outputQueue.retryThreadCount).each { threadCounter ->
            SenderThread senderThread = applicationContext.getBean(SenderThread.class, outputQueue, "_RETRY_" + threadCounter)
            senderThreadRobin.add(senderThread)
            senderThread.start()
        }
    }

    @BlackBox(level = BlackBoxLevel.ERROR, suppressExceptions = true)
    void dbScanRetry() {
        Date lastSendTime = (Instant.now() - Duration.ofSeconds(outputQueue.resendIntervalSeconds)).toDate()
        Integer countToRetry = outputMessageRepository.markForRetry(
                outputQueue.name,
                MessageStatusSets.OUTPUT_RETRY_MESSAGE_STATUSES.value(),
                outputQueue.maxRetryCount,
                lastSendTime,
                PigeonService.staticUUID
        )
        if (countToRetry > 0) {
            LinkedHashSet<OutputMessage> outputMessages = outputMessageRepository.selectForRetry(
                    outputQueue.name,
                    MessageStatusSets.OUTPUT_RETRY_MESSAGE_STATUSES.value(),
                    outputQueue.maxRetryCount,
                    lastSendTime,
                    PigeonService.staticUUID
            )
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
