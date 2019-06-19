package io.infinite.pigeon.threads

import groovy.time.TimeCategory
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.pigeon.conf.OutputQueue
import io.infinite.pigeon.other.MessageStatusSets
import io.infinite.pigeon.springdatarest.entities.OutputMessage
import org.springframework.context.ApplicationContext
import org.springframework.dao.DataAccessException

import java.sql.SQLNonTransientConnectionException
import java.sql.SQLRecoverableException
import java.sql.SQLTransientConnectionException

@BlackBox
@Slf4j
class OutputThreadRetry extends OutputThread {

    OutputThreadRetry(OutputQueue outputQueue, InputThread inputThread, ApplicationContext applicationContext) {
        super(outputQueue, inputThread, applicationContext)
        senderThreadRobin.clear()
        setName(getName() + "_RETRY")
        (1..outputQueue.retryThreadCount).each {
            SenderThread senderThread = new SenderThread(this, it, applicationContext.getEnvironment().getProperty("pigeonOutPluginsDir"))
            senderThread.setName(senderThread.getName() + "_RETRY")
            applicationContext.getAutowireCapableBeanFactory().autowireBean(senderThread)
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

    @BlackBox(level = CarburetorLevel.METHOD)
    void mainCycle() {
        Set<OutputMessage> outputMessages = masterQuery(outputQueue.getName())
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
            } catch (DataAccessException dataAccessException) {
                log.warn("Waiting for recovery of DataAccessException", dataAccessException)
                sleep(outputQueue.recoveryTryPeriodMillisecondsRetry)
            }
        }
    }

}
