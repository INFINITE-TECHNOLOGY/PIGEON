package io.infinite.tpn

import com.fasterxml.jackson.databind.ObjectMapper
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.tpn.conf.Configuration
import io.infinite.tpn.other.Utils
import io.infinite.tpn.threads.MasterThread
import io.infinite.tpn.threads.MasterThreadWithNoRetries
import io.infinite.tpn.threads.MasterThreadWithRetries
import io.infinite.tpn.threads.SplitterThread
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
        runWithLogging()
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    void runWithLogging() {
        Configuration configuration = new ObjectMapper().readValue(new File("./configuration.json").getText(), Configuration.class)
        configuration.queues.each {queue->
            SplitterThread splitterThread = new SplitterThread(queue)
            applicationContext.getAutowireCapableBeanFactory().autowireBean(splitterThread)
            splitterThread.start()
            queue.subscribers.each {subscriber->
                MasterThread masterThread
                if (subscriber.maxRetryCount == 0) {
                    masterThread = new MasterThreadWithNoRetries(subscriber)
                } else {
                    masterThread = new MasterThreadWithRetries(subscriber)
                }
                applicationContext.getAutowireCapableBeanFactory().autowireBean(masterThread)
                masterThread.start()
            }
        }
    }

}
