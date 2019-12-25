package io.infinite.pigeon.threads

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.pigeon.conf.OutputQueue
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.pigeon.other.RoundRobin
import io.infinite.pigeon.springdatarest.entities.OutputMessage
import io.infinite.pigeon.springdatarest.repositories.OutputMessageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

@BlackBox
@Slf4j
abstract class OutputThread extends Thread {

    InputThread inputThread

    OutputQueue outputQueue

    RoundRobin<SenderThread> senderThreadRobin = new RoundRobin<>()

    @Autowired
    OutputMessageRepository outputMessageRepository

    OutputThread(OutputQueue outputQueue, InputThread inputThread, ApplicationContext applicationContext) {
        super(new ThreadGroup("OUTPUT"), outputQueue.name + "_OUTPUT")
        this.outputQueue = outputQueue
        this.inputThread = inputThread
        (1..outputQueue.normalThreadCount).each {
            SenderThread senderThread = new SenderThread(this, it, applicationContext.environment.getProperty("pigeonOutPluginsDir"))
            applicationContext.autowireCapableBeanFactory.autowireBean(senderThread)
            senderThreadRobin.add(senderThread)
            senderThread.start()
        }
    }

    @BlackBox(level = CarburetorLevel.METHOD)
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
