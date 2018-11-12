package io.infinite.tpn.threads

import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.tpn.MessageStatusSets
import io.infinite.tpn.conf.OutputQueue
import io.infinite.tpn.springdatarest.OutputMessage

class OutputThreadNormal extends OutputThread {

    Long lastId

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    OutputThreadNormal(OutputQueue outputQueue) {
        super(outputQueue)
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    LinkedHashSet<OutputMessage> masterQuery(String outputQueueName) {
        return outputMessageRepository.masterQueryNormal(outputQueueName, MessageStatusSets.NO_RETRY_MESSAGE_STATUSES.value(), lastId)
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    void workerEnqueue(OutputMessage outputMessage) {
        super.workerEnqueue(outputMessage)
        lastId = outputMessage.id
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    void run() {
        lastId = outputMessageRepository.getMaxId()
        super.run()
    }

}
