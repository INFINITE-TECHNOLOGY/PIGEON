package io.infinite.tpn.threads

import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.tpn.conf.OutputQueue
import io.infinite.tpn.other.MessageStatuses
import io.infinite.tpn.other.RoundRobin
import io.infinite.tpn.springdatarest.InputMessageRepository
import io.infinite.tpn.springdatarest.OutputMessage
import io.infinite.tpn.springdatarest.OutputMessageRepository
import org.slf4j.MDC
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
        setName(outputQueue.getName() + "_OUTPUT")
        this.outputQueue = outputQueue
        this.inputThread = inputThread
        (1..outputQueue.normalThreadCount).each {
            SenderThread senderThread = new SenderThread(this, it)
            applicationContext.getAutowireCapableBeanFactory().autowireBean(senderThread)
            senderThreadRobin.add(senderThread)
            senderThread.start()
        }
    }

    abstract LinkedHashSet<OutputMessage> masterQuery(String subscriberName)

    @BlackBox(blackBoxLevel = BlackBoxLevel.METHOD_ERROR)
    void workerEnqueue(OutputMessage outputMessage) {
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
    @BlackBox(blackBoxLevel = BlackBoxLevel.METHOD)
    void run() {
        MDC.put("inputQueueName", inputThread.inputQueue.getName())
        while (true) {
            Set<OutputMessage> outputMessages = masterQuery(outputQueue.getName())
            if (outputMessages.size() > 0) {
                outputMessages.each { outputMessage ->
                    workerEnqueue(outputMessage)
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
