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
import io.infinite.pigeon.springdatarest.repositories.OutputMessageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException

import java.sql.SQLNonTransientConnectionException
import java.sql.SQLRecoverableException
import java.sql.SQLTransientConnectionException

@BlackBox
@Slf4j
class InputThread extends Thread {

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

    @BlackBox(level = CarburetorLevel.METHOD)
    void mainCycle() {
        Set<InputMessage> inputMessages = inputMessageRepository.findByInputQueueNameAndStatus(inputQueue.getName(), MessageStatusSets.INPUT_NEW_MESSAGE_STATUSES.value())
        if (inputMessages.size() > 0) {
            inputMessages.each { inputMessage ->
                Set<OutputMessage> outputMessages = new HashSet<>()
                if (inputMessageRepository.findDuplicates(inputMessage.sourceName, inputMessage.inputQueueName, inputMessage.externalId, inputMessage.id, MessageStatuses.SPLIT.value()) == 0) {
                    outputThreadsNormal.each { outputThreadNormal ->
                        OutputMessage outputMessage = new OutputMessage(inputMessage)
                        outputMessage.setOutputQueueName(outputThreadNormal.outputQueue.name)
                        outputMessage.setUrl(outputThreadNormal.outputQueue.url)
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
