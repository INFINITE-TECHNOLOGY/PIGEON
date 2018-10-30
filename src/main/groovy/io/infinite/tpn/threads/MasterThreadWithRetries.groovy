package io.infinite.tpn.threads

import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.tpn.MessageStatusSets
import io.infinite.tpn.conf.Subscriber
import io.infinite.tpn.other.RoundRobin
import io.infinite.tpn.springdatarest.DestinationMessage

class MasterThreadWithRetries extends MasterThread {

    RoundRobin<WorkerRetryThread> workerRetryThreadRoundRobin = new RoundRobin<>()

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    MasterThreadWithRetries(Subscriber subscriber) {
        super(subscriber)
        [1..subscriber.retryThreadCount].each {
            WorkerRetryThread workerRetryThread = new WorkerRetryThread(subscriber)
            workerRetryThreadRoundRobin.add(workerRetryThread)
            workerRetryThread.start()
        }
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    LinkedHashSet<DestinationMessage> masterQuery(String subscriberName) {
        return destinationMessageRepository.masterQueryWithRetries(subscriberName, subscriber.maxRetryCount, subscriber.resendIntervalSeconds)
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    WorkerThread getNextWorkerThread(String messageStatus) {
        if (MessageStatusSets.NORMAL_MESSAGE_STATUSES.value().contains(messageStatus.toLowerCase())) {
            return ++workerNormalThreadRoundRobin.iterator()
        } else {
            return ++workerRetryThreadRoundRobin.iterator()
        }
    }
}
