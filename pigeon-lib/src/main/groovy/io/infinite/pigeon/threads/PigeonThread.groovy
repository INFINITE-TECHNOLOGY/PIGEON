package io.infinite.pigeon.threads

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.pigeon.config.PigeonConf
import io.infinite.pigeon.entities.InputMessage
import io.infinite.pigeon.entities.OutputMessage
import io.infinite.pigeon.other.MessageStatusSets
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.pigeon.repositories.InputMessageRepository
import io.infinite.pigeon.repositories.OutputMessageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.core.io.FileSystemResource
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@ToString(includeNames = true, includeFields = true, includeSuper = true)
@Slf4j
@Component
@BlackBox(level = CarburetorLevel.METHOD)
class PigeonThread extends Thread {

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    OutputMessageRepository outputMessageRepository

    @Autowired
    InputMessageRepository inputMessageRepository

    @Value('${pigeonConfFile}')
    FileSystemResource pigeonConfigResource

    List<InputThread> inputThreads = []

    @PostConstruct
    void init() {
        cleanup()
        log.info("Using Pigeon.json: " + pigeonConfigResource.getFile().getCanonicalPath())
        PigeonConf pigeon = new ObjectMapper().readValue(pigeonConfigResource.getFile().getText(), PigeonConf.class)
        pigeon.inputQueues.each { inputQueue ->
            if (inputQueue.enabled) {
                InputThread inputThread = new InputThread(inputQueue)
                applicationContext.autowireCapableBeanFactory.autowireBean(inputThread)
                inputQueue.outputQueues.each { outputQueue ->
                    if (outputQueue.enabled) {
                        OutputThread outputThreadNormal
                        outputThreadNormal = new OutputThreadNormal(outputQueue, inputThread)
                        applicationContext.getAutowireCapableBeanFactory().autowireBean(outputThreadNormal)
                        outputThreadNormal.start()
                        inputThread.outputThreadsNormal.add(outputThreadNormal)
                        if (outputQueue.maxRetryCount > 0) {
                            OutputThread outputThreadRetry
                            outputThreadRetry = new OutputThreadRetry(outputQueue, inputThread)
                            applicationContext.getAutowireCapableBeanFactory().autowireBean(outputThreadRetry)
                            outputThreadRetry.start()
                            inputThread.outputThreadsRetry.add(outputThreadRetry)
                        }
                    }
                }
                inputThreads.add(inputThread)
            }
        }
    }

    @Override
    void run() {
        inputThreads.each { inputThread ->
            if (inputThread.inputQueue.dbScanEnabled) {
                inputThread.start()
            }
        }
        log.info("Started Pigeon.")
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
