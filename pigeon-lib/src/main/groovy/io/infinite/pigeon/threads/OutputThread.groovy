package io.infinite.pigeon.threads

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
abstract class OutputThread extends Thread {

    InputThread inputThread
    OutputQueue outputQueue
    RoundRobin<SenderThread> senderThreadRobin = new RoundRobin<>()

    @Autowired
    OutputMessageRepository outputMessageRepository

    OutputThread(OutputQueue outputQueue, InputThread inputThread, ApplicationContext applicationContext) {
        super(new ThreadGroup("OUTPUT"), outputQueue.getName() + "_OUTPUT")
        this.outputQueue = outputQueue
        this.inputThread = inputThread
        (1..outputQueue.normalThreadCount).each {
            SenderThread senderThread = new SenderThread(this, it, applicationContext.getEnvironment().getProperty("pigeonOutPluginsDir"))
            applicationContext.getAutowireCapableBeanFactory().autowireBean(senderThread)
            senderThreadRobin.add(senderThread)
            senderThread.start()
        }
    }

    @BlackBox(level = CarburetorLevel.ERROR)
    void senderEnqueue(OutputMessage outputMessage) {
        SenderThread senderThread = ++senderThreadRobin.iterator()
        outputMessage.setStatus(MessageStatuses.WAITING.value())
        outputMessage.setOutputThreadName(getName())
        outputMessage.setLastSenderThreadName(senderThread.getName())
        outputMessageRepository.save(outputMessage)
        senderThread.sendingQueue.put(outputMessage)
        synchronized (senderThread) {
            senderThread.notify()
        }
    }

    @Override
    String toString() {
        return "Thread: " + getName() + "; OutputQueue: " + outputQueue.toString()
    }

}
