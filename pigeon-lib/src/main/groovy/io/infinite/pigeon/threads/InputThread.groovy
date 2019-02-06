package io.infinite.pigeon.threads


import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.pigeon.conf.InputQueue
import io.infinite.pigeon.other.MessageStatusSets
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.pigeon.springdatarest.InputMessage
import io.infinite.pigeon.springdatarest.InputMessageRepository
import io.infinite.pigeon.springdatarest.OutputMessage
import io.infinite.pigeon.springdatarest.OutputMessageRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

@BlackBox
class InputThread extends Thread {

    private final transient Logger log = LoggerFactory.getLogger(this.getClass().getCanonicalName())

    InputQueue inputQueue

    List<OutputThreadNormal> outputThreadsNormal = []

    @Autowired
    InputMessageRepository inputMessageRepository

    @Autowired
    OutputMessageRepository outputMessageRepository

    @BlackBox
    InputThread(InputQueue inputQueue) {
        super(new ThreadGroup("INPUT"), inputQueue.getName() + "_INPUT")
        this.inputQueue = inputQueue
    }

    @Override
    @BlackBox(level = CarburetorLevel.METHOD)
    void run() {
        while (true) {
            Set<InputMessage> inputMessages = inputMessageRepository.findByInputQueueNameAndStatus(inputQueue.getName(), MessageStatusSets.INPUT_NEW_MESSAGE_STATUSES.value())
            if (inputMessages.size() > 0) {
                inputMessages.each { inputMessage ->
                    Set<OutputMessage> outputMessages = new HashSet<>()
                    if (inputMessageRepository.findDuplicates(inputMessage.sourceName, inputMessage.inputQueueName, inputMessage.externalId, inputMessage.id, MessageStatuses.SPLIT.value()) == 0) {
                        outputThreadsNormal.each { outputThreadNormal ->
                            OutputMessage outputMessage = new OutputMessage(inputMessage)
                            outputMessage.setOutputQueueName(outputThreadNormal.getName())
                            outputMessage.setAttemptsCount(outputThreadNormal.getOutputQueue().getMaxRetryCount())
                            outputMessage.setUrl(outputThreadNormal.getOutputQueue().getUrl())
                            outputMessage.setInputMessage(inputMessage)
                            outputMessages.add(outputMessage)
                            if (inputMessage.status != MessageStatuses.RENEWED.value()) {
                                outputMessage.setStatus(MessageStatuses.NEW.value())
                                outputMessageRepository.save(outputMessage)
                                outputThreadNormal.messages.put(outputMessage)
                                synchronized (outputThreadNormal) {
                                    outputThreadNormal.notify()
                                }
                            } else {
                                outputMessage.setStatus(MessageStatuses.RENEWED.value())
                                outputMessageRepository.save(outputMessage)
                            }
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
            sleep(inputQueue.pollPeriodMilliseconds)
        }
    }

    @Override
    String toString() {
        return "Thread: " + getName() + "; InputQueue: " + inputQueue.toString()
    }

}
