package io.infinite.pigeon.threads

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.pigeon.config.OutputQueue
import io.infinite.pigeon.entities.OutputMessage
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.pigeon.other.RoundRobin
import io.infinite.pigeon.repositories.OutputMessageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@BlackBox(level = BlackBoxLevel.METHOD)
@Slf4j
@ToString(includeNames = true, includeFields = true, includeSuper = true)
@Component
@Scope("prototype")
abstract class OutputThread extends Thread {

    OutputQueue outputQueue

    RoundRobin<SenderThread> senderThreadRobin = new RoundRobin<>()

    @Autowired
    OutputMessageRepository outputMessageRepository

    @Autowired
    ApplicationContext applicationContext

    OutputThread(OutputQueue outputQueue) {
        super(new ThreadGroup("OUTPUT"), outputQueue.name + "_OUTPUT")
        this.outputQueue = outputQueue
    }

    @PostConstruct
    void initSenderThreads() {
        (1..outputQueue.normalThreadCount).each { threadCounter ->
            SenderThread senderThread = applicationContext.getBean(SenderThread.class, outputQueue, "_" + threadCounter)
            senderThreadRobin.add(senderThread)
            senderThread.start()
        }
    }

    @BlackBox(level = BlackBoxLevel.METHOD, suppressExceptions = true)
    void senderEnqueue(OutputMessage outputMessage) {
        SenderThread senderThread = ++senderThreadRobin.iterator()
        outputMessage.status = MessageStatuses.WAITING.value()
        outputMessage.lastSenderThreadName = senderThread.name
        outputMessage = outputMessageRepository.saveAndFlush(outputMessage)
        senderThread.sendingQueue.put(outputMessage)
        synchronized (senderThread) {
            senderThread.notify()
        }
    }

    @Override
    String toString() {
        return "Thread: " + name + "; OutputQueue: " + outputQueue.toString()
    }

}
