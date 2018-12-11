package io.infinite.pigeon.threads

import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.pigeon.conf.OutputQueue
import io.infinite.pigeon.other.MessageStatusSets
import io.infinite.pigeon.springdatarest.OutputMessage
import org.springframework.context.ApplicationContext

@BlackBox
class OutputThreadNormal extends OutputThread {

    Long lastId

    OutputThreadNormal(OutputQueue outputQueue, InputThread inputThread, ApplicationContext applicationContext) {
        super(outputQueue, inputThread, applicationContext)
    }

    @Override
    @BlackBox(blackBoxLevel = BlackBoxLevel.METHOD_ERROR)
    LinkedHashSet<OutputMessage> masterQuery(String outputQueueName) {
        return outputMessageRepository.masterQueryNormal(outputQueueName, MessageStatusSets.NO_RETRY_MESSAGE_STATUSES.value(), lastId)
    }

    @Override
    void workerEnqueue(OutputMessage outputMessage) {
        super.workerEnqueue(outputMessage)
        lastId = outputMessage.id
    }

    @Override
    void run() {
        lastId = outputMessageRepository.getMaxId()
        super.run()
    }

}
