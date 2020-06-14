package io.infinite.pigeon.springdatarest.services

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.pigeon.conf.PigeonConf
import io.infinite.pigeon.springdatarest.entities.InputMessage
import io.infinite.pigeon.springdatarest.entities.OutputMessage
import io.infinite.pigeon.other.MessageStatusSets
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.pigeon.springdatarest.repositories.InputMessageRepository
import io.infinite.pigeon.springdatarest.repositories.OutputMessageRepository
import io.infinite.pigeon.threads.InputThread
import io.infinite.pigeon.threads.OutputThreadNormal
import io.infinite.pigeon.threads.OutputThreadRetry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.core.io.FileSystemResource
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

@ToString(includeNames = true, includeFields = true)
@Slf4j
@Service
@BlackBox(level = BlackBoxLevel.METHOD)
class PigeonService {

    static UUID staticUUID = UUID.randomUUID()

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    OutputMessageRepository outputMessageRepository

    @Autowired
    InputMessageRepository inputMessageRepository

    @Value('${pigeonConfFile}')
    FileSystemResource pigeonConfigResource

    Map<String, InputThread> inputThreadsByName = [:]

    @PostConstruct
    void init() {
        cleanup()
        log.info("Using Pigeon.json: " + pigeonConfigResource.getFile().getCanonicalPath())
        PigeonConf pigeon = new ObjectMapper().readValue(pigeonConfigResource.getFile().getText(), PigeonConf.class)
        pigeon.inputQueues.each { inputQueue ->
            if (inputQueue.enabled) {
                InputThread inputThread = applicationContext.getBean(InputThread.class, inputQueue)
                inputQueue.outputQueues.each { outputQueue ->
                    if (outputQueue.enabled) {
                        OutputThreadNormal outputThreadNormal = applicationContext.getBean(OutputThreadNormal.class, outputQueue)
                        outputThreadNormal.start()
                        inputThread.outputThreadsNormal.add(outputThreadNormal)
                        if (outputQueue.maxRetryCount > 0) {
                            OutputThreadRetry outputThreadRetry = applicationContext.getBean(OutputThreadRetry.class, outputQueue)
                            outputThreadRetry.start()
                            inputThread.outputThreadsRetry.add(outputThreadRetry)
                        }
                    }
                }
                inputThreadsByName.put(inputThread.inputQueue.name, inputThread)
                inputThread.start()
            }
        }
    }

    void cleanup() {
        Set<InputMessage> delayedMessages = inputMessageRepository.findByMessageStatusList(MessageStatusSets.INPUT_RENEW_MESSAGE_STATUSES.value())
        delayedMessages.each {
            it.status = MessageStatuses.RENEWED.value()
        }
        inputMessageRepository.saveAll(delayedMessages)
        inputMessageRepository.flush()
        Set<OutputMessage> renewedMessages = outputMessageRepository.findByMessageStatusList(MessageStatusSets.OUTPUT_RENEW_MESSAGE_STATUSES.value())
        renewedMessages.each {
            it.status = MessageStatuses.RENEWED.value()
        }
        outputMessageRepository.saveAll(renewedMessages)
        outputMessageRepository.flush()
    }

}
