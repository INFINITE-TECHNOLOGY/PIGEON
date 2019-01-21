package io.infinite.pigeon.threads

import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.pigeon.conf.OutputQueue
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.pigeon.other.RoundRobin
import io.infinite.pigeon.springdatarest.InputMessageRepository
import io.infinite.pigeon.springdatarest.OutputMessage
import io.infinite.pigeon.springdatarest.OutputMessageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

@BlackBox
abstract class OutputThread extends Thread {

    InputThread inputThread
    OutputQueue outputQueue
    RoundRobin<SenderThread> senderThreadRobin = new RoundRobin<>()

    @Autowired
    OutputMessageRepository outputMessageRepository

    @Autowired
    InputMessageRepository inputMessageRepository

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

    abstract LinkedHashSet<OutputMessage> masterQuery(String subscriberName)

    @BlackBox(level = CarburetorLevel.ERROR)
    void senderEnqueue(OutputMessage outputMessage) {
        SenderThread senderThread = ++senderThreadRobin.iterator()
        outputMessage.setStatus(MessageStatuses.WAITING.value())
        outputMessage.setThreadName(senderThread.getName())
        outputMessageRepository.save(outputMessage)
        senderThread.getSendingQueue().put(outputMessage)
        synchronized (senderThread) {
            senderThread.notify()
        }
    }

    @Override
    @BlackBox(level = CarburetorLevel.METHOD)
    void run() {
        while (true) {
            Set<OutputMessage> outputMessages = masterQuery(outputQueue.getName())
            if (outputMessages.size() > 0) {
                outputMessages.each { outputMessage ->
                    senderEnqueue(outputMessage)
                }
            }
            sleep(outputQueue.pollPeriodMilliseconds)
        }
    }

    @Override
    String toString() {
        return "Thread: " + getName() + "; OutputQueue: " + outputQueue.toString()
    }

}
