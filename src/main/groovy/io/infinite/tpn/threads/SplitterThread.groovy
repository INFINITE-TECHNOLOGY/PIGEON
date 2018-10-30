package io.infinite.tpn.threads

import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.tpn.AppicationProperties
import io.infinite.tpn.MessageStatus
import io.infinite.tpn.conf.Queue
import io.infinite.tpn.springdatarest.DestinationMessage
import io.infinite.tpn.springdatarest.DestinationMessageRepository
import io.infinite.tpn.springdatarest.SourceMessage
import io.infinite.tpn.springdatarest.SourceMessageRepository
import org.springframework.beans.factory.annotation.Autowired

class SplitterThread extends Thread {

    Queue queue

    @Autowired
    SourceMessageRepository sourceMessageRepository

    @Autowired
    DestinationMessageRepository destinationMessageRepository

    @Autowired
    AppicationProperties applicationProperties

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    SplitterThread(Queue queue) {
        this.queue = queue
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    void run() {
        while (true) {
            Set<SourceMessage> sourceMessages = sourceMessageRepository.findByQueueNameAndStatus(queue.getName(), MessageStatus.NEW.value())
            if (sourceMessages.size() > 0) {
                Set<DestinationMessage> destinationMessages = new HashSet<>()
                sourceMessages.each { sourceMessage ->
                    queue.subscribers.each { subscriber ->
                        DestinationMessage destinationMessage = new DestinationMessage(sourceMessage)
                        destinationMessage.setSubscriberName(subscriber.getName())
                        destinationMessage.setRetryCount(subscriber.getMaxRetryCount())
                        destinationMessage.setUrl(subscriber.getUrl())
                        destinationMessage.setStatus(MessageStatus.WAITING_FOR_MASTER.value())
                        destinationMessages.add(destinationMessage)
                    }
                    sourceMessage.setStatus(MessageStatus.SPLIT.value())
                }
                //todo: make transactional
                sourceMessageRepository.save(sourceMessages)
                destinationMessageRepository.save(destinationMessages)
            }
            sleep(applicationProperties.splitterPollPeriodMilliseconds)
        }
    }

}
