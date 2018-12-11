package io.infinite.pigeon.threads

import groovy.time.TimeCategory
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.pigeon.conf.OutputQueue
import io.infinite.pigeon.other.MessageStatusSets
import io.infinite.pigeon.springdatarest.OutputMessage
import org.springframework.context.ApplicationContext

@BlackBox
class OutputThreadRetry extends OutputThread {

    OutputThreadRetry(OutputQueue outputQueue, InputThread inputThread, ApplicationContext applicationContext) {
        super(outputQueue, inputThread, applicationContext)
        setName(getName() + "_RETRY")
        (1..outputQueue.retryThreadCount).each {
            SenderThread senderThread = new SenderThread(this, it)
            senderThreadRobin.add(senderThread)
            senderThread.start()
        }
    }

    @Override
    @BlackBox(blackBoxLevel = BlackBoxLevel.METHOD)
    LinkedHashSet<OutputMessage> masterQuery(String outputQueueName) {
        Date maxLastSendDate
        use(TimeCategory) {
            maxLastSendDate = (new Date() - outputQueue.resendIntervalSeconds.seconds)
        }
        return outputMessageRepository.masterQueryRetry(outputQueueName, MessageStatusSets.RETRY_MESSAGE_STATUSES.value(), outputQueue.maxRetryCount, maxLastSendDate)
    }

    @Override
    void run() {
        super.run()
    }

}
