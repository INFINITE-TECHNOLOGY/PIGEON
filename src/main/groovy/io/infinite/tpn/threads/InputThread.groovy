package io.infinite.tpn.threads

import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.tpn.AppicationProperties
import io.infinite.tpn.MessageStatuses
import io.infinite.tpn.conf.InputQueue
import io.infinite.tpn.springdatarest.OutputMessage
import io.infinite.tpn.springdatarest.OutputMessageRepository
import io.infinite.tpn.springdatarest.InputMessage
import io.infinite.tpn.springdatarest.InputMessageRepository
import org.springframework.beans.factory.annotation.Autowired

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
        setName(inputQueue.getName())
        this.inputQueue = inputQueue
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    void run() {
        while (true) {
            Set<InputMessage> inputMessages = inputMessageRepository.findByInputQueueNameAndStatus(inputQueue.getName(), MessageStatuses.NEW.value())
            if (inputMessages.size() > 0) {
                Set<OutputMessage> outputMessages = new HashSet<>()
                inputMessages.each { inputMessage ->
                    inputQueue.outputQueues.each { outputQueue ->
                        OutputMessage outputMessage = new OutputMessage(inputMessage)
                        outputMessage.setOutputQueueName(outputQueue.getName())
                        outputMessage.setRetryCount(outputQueue.getMaxRetryCount())
                        outputMessage.setUrl(outputQueue.getUrl())
                        outputMessage.setStatus(MessageStatuses.NEW.value())
                        outputMessage.setInputMessage(inputMessage)
                        outputMessages.add(outputMessage)
                    }
                    inputMessage.getOutputMessages().addAll(outputMessages)
                    inputMessage.setStatus(MessageStatuses.SPLIT.value())
                }
                inputMessageRepository.save(inputMessages)
            }
            sleep(applicationProperties.inputThreadPollPeriodMilliseconds)
        }
    }

}
