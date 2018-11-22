package io.infinite.tpn.threads

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.tpn.AppicationProperties
import io.infinite.tpn.conf.InputQueue
import io.infinite.tpn.other.MessageStatuses
import io.infinite.tpn.springdatarest.InputMessage
import io.infinite.tpn.springdatarest.InputMessageRepository
import io.infinite.tpn.springdatarest.OutputMessage
import io.infinite.tpn.springdatarest.OutputMessageRepository
import org.springframework.beans.factory.annotation.Autowired

@Slf4j
@BlackBox
class InputThread extends Thread {

    InputQueue inputQueue

    @Autowired
    InputMessageRepository inputMessageRepository

    @Autowired
    OutputMessageRepository outputMessageRepository

    @Autowired
    AppicationProperties applicationProperties

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    InputThread(InputQueue inputQueue) {
        setName("Input_" + inputQueue.getName())
        this.inputQueue = inputQueue
    }

    @Override
    void run() {
        while (true) {
            Set<InputMessage> inputMessages = inputMessageRepository.findByInputQueueNameAndStatus(inputQueue.getName(), MessageStatuses.NEW.value())
            if (inputMessages.size() > 0) {
                Set<OutputMessage> outputMessages = new HashSet<>()
                inputMessages.each { inputMessage ->
                    if (inputMessageRepository.findDuplicates(inputMessage.sourceName, inputMessage.inputQueueName, inputMessage.externalId, inputMessage.id, MessageStatuses.SPLIT.value()) == 0) {
                        inputQueue.outputQueues.each { outputQueue ->
                            OutputMessage outputMessage = new OutputMessage(inputMessage)
                            outputMessage.setOutputQueueName(outputQueue.getName())
                            outputMessage.setAttemptsCount(outputQueue.getMaxRetryCount())
                            outputMessage.setUrl(outputQueue.getUrl())
                            outputMessage.setStatus(MessageStatuses.NEW.value())
                            outputMessage.setInputMessage(inputMessage)
                            outputMessages.add(outputMessage)
                        }
                        inputMessage.getOutputMessages().addAll(outputMessages)
                        inputMessage.setStatus(MessageStatuses.SPLIT.value())
                    } else {
                        log.warn(String.format("Input Message with same externalId %s and different id already exists in status %s for new message with id %s for source %s and inputQueueName %s", inputMessage.externalId, MessageStatuses.SPLIT.value(), inputMessage.id, inputMessage.sourceName, inputMessage.inputQueueName))
                        inputMessage.setStatus(MessageStatuses.DUPLICATE.value())
                    }
                    inputMessageRepository.save(inputMessage)
                }
            }
            sleep(applicationProperties.inputThreadPollPeriodMilliseconds)
        }
    }

}
