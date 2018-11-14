package io.infinite.tpn

import com.fasterxml.jackson.databind.ObjectMapper
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.tpn.conf.Configuration
import io.infinite.tpn.threads.InputThread
import io.infinite.tpn.threads.OutputThread
import io.infinite.tpn.threads.OutputThreadNormal
import io.infinite.tpn.threads.OutputThreadRetry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ApplicationContext
import org.springframework.hateoas.config.EnableHypermediaSupport

@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@SpringBootApplication
class App implements CommandLineRunner {

    @Autowired
    AppicationProperties appicationProperties

    @Autowired
    ApplicationContext applicationContext

    static void main(String[] args) {
        SpringApplication.run(App.class, args)
    }

    @Override
    void run(String... args) throws Exception {
        System.setProperty("blackbox.mode", appicationProperties.blackboxMode)
        Thread.currentThread().setName("App")
        runWithLogging()
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    void runWithLogging() {
        Configuration configuration = new ObjectMapper().readValue(new File("./configuration.json").getText(), Configuration.class)
        configuration.inputQueues.each { inputQueue ->
            InputThread inputThread = new InputThread(inputQueue)
            applicationContext.getAutowireCapableBeanFactory().autowireBean(inputThread)
            inputThread.start()
            inputQueue.outputQueues.each { outputQueue ->
                OutputThread outputThreadNormal
                outputThreadNormal = new OutputThreadNormal(outputQueue, applicationContext)
                applicationContext.getAutowireCapableBeanFactory().autowireBean(outputThreadNormal)
                outputThreadNormal.start()
                if (outputQueue.maxRetryCount > 0) {
                    OutputThread outputThreadRetry
                    outputThreadRetry = new OutputThreadRetry(outputQueue, applicationContext)
                    applicationContext.getAutowireCapableBeanFactory().autowireBean(outputThreadRetry)
                    outputThreadRetry.start()
                }
            }
        }
    }

}
