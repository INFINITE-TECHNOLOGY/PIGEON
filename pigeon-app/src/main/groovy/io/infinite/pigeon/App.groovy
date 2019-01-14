package io.infinite.pigeon

import com.fasterxml.jackson.databind.ObjectMapper
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.pigeon.conf.Configuration
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.pigeon.springdatarest.OutputMessage
import io.infinite.pigeon.springdatarest.OutputMessageRepository
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
class App implements CommandLineRunner {

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    OutputMessageRepository outputMessageRepository

    @Value('${pigeonConfFile}')
    FileSystemResource pigeonConfigResource

    static void main(String[] args) {
        SpringApplication.run(App.class, args)
    }

    @Override
    void run(String... args) throws Exception {
        runWithLogging()
    }

    @BlackBox(level = CarburetorLevel.EXPRESSION)
    void runWithLogging() {
        Configuration configuration = new ObjectMapper().readValue(pigeonConfigResource.getFile().getText(), Configuration.class)
        Set<OutputMessage> waitingMessages = outputMessageRepository.findByStatus(MessageStatuses.WAITING.value())
        waitingMessages.each {
            it.status = MessageStatuses.RENEWED.value()
        }
        outputMessageRepository.saveAll(waitingMessages)
        configuration.inputQueues.each { inputQueue ->
            InputThread inputThread = new InputThread(inputQueue)
            applicationContext.getAutowireCapableBeanFactory().autowireBean(inputThread)
            inputThread.start()
            inputQueue.outputQueues.each { outputQueue ->
                OutputThread outputThreadNormal
                outputThreadNormal = new OutputThreadNormal(outputQueue, inputThread, applicationContext)
                applicationContext.getAutowireCapableBeanFactory().autowireBean(outputThreadNormal)
                outputThreadNormal.start()
                if (outputQueue.maxRetryCount > 0) {
                    OutputThread outputThreadRetry
                    outputThreadRetry = new OutputThreadRetry(outputQueue, inputThread, applicationContext)
                    applicationContext.getAutowireCapableBeanFactory().autowireBean(outputThreadRetry)
                    outputThreadRetry.start()
                }
            }
        }
    }

}
