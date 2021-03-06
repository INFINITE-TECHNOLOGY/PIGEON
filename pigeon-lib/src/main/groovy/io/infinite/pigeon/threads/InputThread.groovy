package io.infinite.pigeon.threads

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.pigeon.conf.InputQueue
import io.infinite.pigeon.springdatarest.entities.InputMessage
import io.infinite.pigeon.springdatarest.entities.OutputMessage
import io.infinite.pigeon.other.MessageStatusSets
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.pigeon.springdatarest.repositories.InputMessageRepository
import io.infinite.pigeon.springdatarest.services.PigeonService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@BlackBox(level = BlackBoxLevel.METHOD)
@Slf4j
@ToString(includeNames = true, includeFields = true, includeSuper = true)
@Component
@Scope("prototype")
class InputThread extends Thread {

    InputQueue inputQueue

    List<OutputThreadNormal> outputThreadsNormal = []

    List<OutputThreadRetry> outputThreadsRetry = []

    @Autowired
    InputMessageRepository inputMessageRepository

    InputThread(InputQueue inputQueue) {
        super(new ThreadGroup("INPUT"), inputQueue.name + "_INPUT")
        this.inputQueue = inputQueue
    }

    @BlackBox(level = BlackBoxLevel.ERROR, suppressExceptions = true)
    void dbScanSplit() {
        Integer countToSplit = inputMessageRepository.markForSplit(
                inputQueue.name,
                MessageStatusSets.INPUT_NEW_MESSAGE_STATUSES.value(),
                PigeonService.staticUUID
        )
        if (countToSplit > 0) {
            LinkedHashSet<InputMessage> inputMessages = inputMessageRepository.selectForSplit(
                    inputQueue.name,
                    MessageStatusSets.INPUT_NEW_MESSAGE_STATUSES.value(),
                    PigeonService.staticUUID
            )
            inputMessages.each { inputMessage ->
                splitInput(inputMessage)
            }
        }
    }

    @BlackBox(level = BlackBoxLevel.ERROR)
    OutputMessage createOutputMessage(InputMessage inputMessage, OutputThread outputThread, MessageStatuses messageStatus) {
        OutputMessage outputMessage = new OutputMessage(inputMessage)
        outputMessage.outputQueueName = outputThread.outputQueue.name
        outputMessage.outputThreadName = outputThread.name
        outputMessage.url = outputThread.outputQueue.url
        outputMessage.status = messageStatus.value()
        return outputMessage
    }

    @BlackBox(level = BlackBoxLevel.METHOD)
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
    @BlackBox(level = BlackBoxLevel.METHOD)
    void run() {
        while (inputQueue.enableScanDB) {//use same approach as in output retry thread
            dbScanSplit()
            sleep(inputQueue.pollPeriodMilliseconds)
        }
    }

    @Override
    String toString() {
        return "Thread: " + getName() + "; InputQueue: " + inputQueue.toString()
    }

}
