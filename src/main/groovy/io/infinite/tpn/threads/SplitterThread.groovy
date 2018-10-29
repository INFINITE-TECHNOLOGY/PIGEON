package io.infinite.tpn.threads

import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.tpn.AppicationProperties
import io.infinite.tpn.MessageStatus
import io.infinite.tpn.conf.Queue
import io.infinite.tpn.db.DestinationMessage
import io.infinite.tpn.db.DestinationMessageRepository
import io.infinite.tpn.db.SourceMessage
import io.infinite.tpn.db.SourceMessageRepository
import org.springframework.beans.factory.annotation.Autowired

class SplitterThread extends Thread {

    Queue channel

    @Autowired
    SourceMessageRepository sourceMessageRepository

    @Autowired
    DestinationMessageRepository destinationMessageRepository

    @Autowired
    AppicationProperties applicationProperties

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    SplitterThread(Queue channel) {
        this.channel = channel
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    void run() {
        while (true) {
            Set<SourceMessage> sourceMessages = sourceMessageRepository.findByQueueNameAndStatus(channel.getName(), MessageStatus.NEW.value())
            if (sourceMessages.size() > 0) {
                Set<DestinationMessage> destinationMessages = new HashSet<>()
                sourceMessages.each {sourceMessage->
                    channel.subscribers.each {subscriber->
                        DestinationMessage destinationMessage = new DestinationMessage(sourceMessage)
                        destinationMessage.setSubscriberName(subscriber.getName())
                        destinationMessage.setRetryCount(subscriber.getRetryCount())
                        destinationMessage.setUrl(subscriber.getUrl())
                        destinationMessage.setStatus(MessageStatus.READY_FOR_SENDING.value())
                        destinationMessages.add(destinationMessage)
                    }
                    sourceMessage.setStatus(MessageStatus.SPLITTED.value())
                }
                sourceMessageRepository.save(sourceMessages)
                destinationMessageRepository.save(destinationMessages)
            }
            sleep(applicationProperties.splitterPollPeriodMilliseconds)
        }
    }

}
