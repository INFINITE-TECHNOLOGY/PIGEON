package io.infinite.pigeon

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.conf.Configuration
import io.infinite.pigeon.other.MessageStatusSets
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.pigeon.springdatarest.entities.InputMessage
import io.infinite.pigeon.springdatarest.entities.OutputMessage
import io.infinite.pigeon.springdatarest.repositories.InputMessageRepository
import io.infinite.pigeon.springdatarest.repositories.OutputMessageRepository
import io.infinite.pigeon.threads.InputThread
import io.infinite.pigeon.threads.OutputThread
import io.infinite.pigeon.threads.OutputThreadNormal
import io.infinite.pigeon.threads.OutputThreadRetry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ApplicationContext
import org.springframework.core.io.FileSystemResource
import org.springframework.hateoas.config.EnableHypermediaSupport

@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@SpringBootApplication
@Slf4j
class App implements CommandLineRunner {

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    OutputMessageRepository outputMessageRepository

    @Autowired
    InputMessageRepository inputMessageRepository

    @Value('${pigeonConfFile}')
    FileSystemResource pigeonConfigResource

    static void main(String[] args) {
        SpringApplication.run(App.class, args)
    }

    @Override
    void run(String... args) throws Exception {
        runWithLogging()
    }

    @BlackBox
    void runWithLogging() {
        log.debug("Using Pigeon.json: " + pigeonConfigResource.getFile().getCanonicalPath())
        Configuration configuration = new ObjectMapper().readValue(pigeonConfigResource.getFile().getText(), Configuration.class)
        Set<InputMessage> delayedMessages = inputMessageRepository.findByMessageStatusList(MessageStatusSets.INPUT_RENEW_MESSAGE_STATUSES.value())
        delayedMessages.each {
            it.status = MessageStatuses.RENEWED.value()
        }
        inputMessageRepository.saveAll(delayedMessages)
        Set<OutputMessage> renewedMessages = outputMessageRepository.findByMessageStatusList(MessageStatusSets.OUTPUT_RENEW_MESSAGE_STATUSES.value())
        renewedMessages.each {
            it.status = MessageStatuses.RENEWED.value()
        }
        outputMessageRepository.saveAll(renewedMessages)
        configuration.inputQueues.each { inputQueue ->
            if (inputQueue.enabled) {
                InputThread inputThread = new InputThread(inputQueue)
                applicationContext.getAutowireCapableBeanFactory().autowireBean(inputThread)
                inputQueue.outputQueues.each { outputQueue ->
                    if (outputQueue.enabled) {
                        OutputThread outputThreadNormal
                        outputThreadNormal = new OutputThreadNormal(outputQueue, inputThread, applicationContext)
                        applicationContext.getAutowireCapableBeanFactory().autowireBean(outputThreadNormal)
                        outputThreadNormal.start()
                        inputThread.outputThreadsNormal.add(outputThreadNormal)
                        if (outputQueue.maxRetryCount > 0) {
                            OutputThread outputThreadRetry
                            outputThreadRetry = new OutputThreadRetry(outputQueue, inputThread, applicationContext)
                            applicationContext.getAutowireCapableBeanFactory().autowireBean(outputThreadRetry)
                            outputThreadRetry.start()
                        }
                    }
                }
                inputThread.start()
            }
        }
    }

}
