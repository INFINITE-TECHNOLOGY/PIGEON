package io.infinite.pigeon.threads

import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
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
    @BlackBox(level = CarburetorLevel.ERROR)
    LinkedHashSet<OutputMessage> masterQuery(String outputQueueName) {
        return outputMessageRepository.masterQueryNormal(outputQueueName, MessageStatusSets.OUTPUT_NORMAL_MESSAGE_STATUSES.value(), lastId)
    }

    @Override
    void senderEnqueue(OutputMessage outputMessage) {
        super.senderEnqueue(outputMessage)
        lastId = outputMessage.id
    }

    @Override
    void run() {
        lastId = outputMessageRepository.getMaxId()
        super.run()
    }

}
