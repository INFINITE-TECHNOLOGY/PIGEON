package io.infinite.pigeon.threads

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.pigeon.conf.InputQueue
import io.infinite.pigeon.other.MessageStatusSets
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.pigeon.springdatarest.entities.InputMessage
import io.infinite.pigeon.springdatarest.entities.OutputMessage
import io.infinite.pigeon.springdatarest.repositories.InputMessageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException

@BlackBox
@Slf4j
class InputThread extends Thread {

    InputQueue inputQueue

    List<OutputThreadNormal> outputThreadsNormal = []

    List<OutputThreadRetry> outputThreadsRetry = []

    @Autowired
    InputMessageRepository inputMessageRepository

    @BlackBox
    InputThread(InputQueue inputQueue) {
        super(new ThreadGroup("INPUT"), inputQueue.name + "_INPUT")
        this.inputQueue = inputQueue
    }

    @BlackBox(level = CarburetorLevel.ERROR)
    void mainCycle() {
        Set<InputMessage> inputMessages = inputMessageRepository.findByInputQueueNameAndStatus(inputQueue.name, MessageStatusSets.INPUT_NEW_MESSAGE_STATUSES.value())
        if (inputMessages.size() > 0) {
            inputMessages.each { inputMessage ->
                splitInput(inputMessage)
            }
        }
    }

    @BlackBox(level = CarburetorLevel.ERROR)
    OutputMessage createOutputMessage(InputMessage inputMessage, OutputThread outputThread, MessageStatuses messageStatus) {
        OutputMessage outputMessage = new OutputMessage(inputMessage)
        outputMessage.outputQueueName = outputThread.outputQueue.name
        outputMessage.outputThreadName = outputThread.name
        outputMessage.url = outputThread.outputQueue.url
        outputMessage.status = messageStatus.value()
        return outputMessage
    }

    @BlackBox(level = CarburetorLevel.METHOD)
    void splitInput(InputMessage inputMessage) {
        if (inputMessageRepository.findDuplicates(inputMessage.sourceName, inputMessage.inputQueueName, inputMessage.externalId, inputMessage.id, MessageStatuses.SPLIT.value()) == 0) {
            if (inputMessage.status != MessageStatuses.RENEWED.value()) {
                outputThreadsNormal.each { outputThreadNormal ->
                    inputMessage.outputMessages.add(
                            createOutputMessage(inputMessage, outputThreadNormal, MessageStatuses.NEW)
                    )
                }
            } else {
                outputThreadsRetry.each { outputThreadRetry ->
                    inputMessage.outputMessages.add(
                            createOutputMessage(inputMessage, outputThreadRetry, MessageStatuses.RENEWED)
                    )
                }
            }
            inputMessage.status = MessageStatuses.SPLIT.value()
        } else {
            log.warn(String.format("Input Message with same externalId %s and different id already exists in status %s for new message with id %s for source %s and inputQueueName %s", inputMessage.externalId, MessageStatuses.SPLIT.value(), inputMessage.id, inputMessage.sourceName, inputMessage.inputQueueName))
            inputMessage.status = MessageStatuses.DUPLICATE.value()
        }
        inputMessage = inputMessageRepository.saveAndFlush(inputMessage)
        inputMessage.outputMessages.each { outputMessage ->
            outputThreadsNormal.each { outputThreadNormal ->
                if (outputMessage.outputThreadName == outputThreadNormal.name) {
                    outputThreadNormal.outputMessages.put(outputMessage)
                    synchronized (outputThreadNormal) {
                        outputThreadNormal.notify()
                    }
                }
            }
        }
    }

    @Override
    @BlackBox(level = CarburetorLevel.METHOD)
    void run() {
        while (true) {
            try {
                mainCycle()
                sleep(inputQueue.pollPeriodMilliseconds)
            } catch (DataAccessException dataAccessException) {
                log.warn("Waiting for recovery of DataAccessException", dataAccessException)
                sleep(inputQueue.recoveryTryPeriodMilliseconds)
            }
        }
    }

    @Override
    String toString() {
        return "Thread: " + getName() + "; InputQueue: " + inputQueue.toString()
    }

}
