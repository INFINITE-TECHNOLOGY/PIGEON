package io.infinite.tpn.threads

import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.tpn.AppicationProperties
import io.infinite.tpn.MessageStatus
import io.infinite.tpn.conf.Subscriber
import io.infinite.tpn.other.RoundRobin
import io.infinite.tpn.springdatarest.DestinationMessage
import io.infinite.tpn.springdatarest.DestinationMessageRepository
import io.infinite.tpn.springdatarest.SourceMessageRepository
import org.springframework.beans.factory.annotation.Autowired

abstract class MasterThread extends Thread {

    Subscriber subscriber
    RoundRobin<WorkerNormalThread> workerNormalThreadRoundRobin = new RoundRobin<>()

    @Autowired
    DestinationMessageRepository destinationMessageRepository

    @Autowired
    SourceMessageRepository sourceMessageRepository

    @Autowired
    AppicationProperties applicationProperties

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    MasterThread(Subscriber subscriber) {
        this.subscriber = subscriber
        [1..subscriber.normalThreadCount].each {
            WorkerNormalThread workerNormalThread = new WorkerNormalThread(subscriber)
            workerNormalThreadRoundRobin.add(workerNormalThread)
            workerNormalThread.start()
        }
    }

    abstract LinkedHashSet<DestinationMessage> masterQuery(String subscriberName)

    abstract WorkerThread getNextWorkerThread(String messageStatus)

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    void workerEnqueue(DestinationMessage destinationMessage) {
        WorkerThread workerThread = getNextWorkerThread(destinationMessage.status)
        destinationMessage.setStatus(MessageStatus.WAITING_FOR_WORKER.value())
        destinationMessage.setThreadName(workerThread.getName())
        destinationMessageRepository.save(destinationMessage)
        workerThread.getDestinationMessageQueue().put(destinationMessage)
        synchronized (workerThread) {
            workerThread.notify()
        }
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    void run() {
        while (true) {
            Set<DestinationMessage> destinationMessages = masterQuery(subscriber.getName())
            if (destinationMessages.size() > 0) {
                destinationMessages.each { destinationMessage ->
                    if (isNotDuplicate(destinationMessage.sourceMessage.sourceName, destinationMessage.sourceMessage.externalId, destinationMessage.sourceMessage.id, destinationMessage.sourceMessage.queueName)) {
                        workerEnqueue(destinationMessage)
                    } else {
                        destinationMessage.setStatus(MessageStatus.DUPLICATE.value())
                        destinationMessageRepository.save(destinationMessage)
                    }
                }
                destinationMessageRepository.save(destinationMessages)
            }
            sleep(applicationProperties.splitterPollPeriodMilliseconds)
        }
    }

    Boolean isNotDuplicate(String sourceName, String externalId, Long id, String queueName) {
        //todo - should be in Splitter thread
    }
}
