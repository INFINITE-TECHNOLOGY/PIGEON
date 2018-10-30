package io.infinite.tpn.threads

import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.tpn.conf.Subscriber
import io.infinite.tpn.springdatarest.DestinationMessage

class MasterThreadWithNoRetries extends MasterThread {

    Long minTpnId

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    MasterThreadWithNoRetries(Subscriber subscriber) {
        super(subscriber)
        minTpnId = destinationMessageRepository.getMaxId()
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    LinkedHashSet<DestinationMessage> masterQuery(String subscriberName) {
        return destinationMessageRepository.masterQueryWithNoRetries(subscriberName, minTpnId)
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    WorkerThread getNextWorkerThread(String messageStatus) {
        return ++workerNormalThreadRoundRobin
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    void workerEnqueue(DestinationMessage destinationMessage) {
        super.workerEnqueue(destinationMessage)
        minTpnId = destinationMessage.id
    }
}
