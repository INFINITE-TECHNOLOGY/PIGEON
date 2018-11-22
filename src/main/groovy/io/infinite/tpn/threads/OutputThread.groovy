package io.infinite.tpn.threads

import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.tpn.AppicationProperties
import io.infinite.tpn.conf.OutputQueue
import io.infinite.tpn.other.MessageStatuses
import io.infinite.tpn.other.RoundRobin
import io.infinite.tpn.springdatarest.InputMessageRepository
import io.infinite.tpn.springdatarest.OutputMessage
import io.infinite.tpn.springdatarest.OutputMessageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

@BlackBox
abstract class OutputThread extends Thread {

    OutputQueue outputQueue
    RoundRobin<SenderThread> senderThreadRobin = new RoundRobin<>()

    @Autowired
    OutputMessageRepository outputMessageRepository

    @Autowired
    InputMessageRepository inputMessageRepository

    @Autowired
    AppicationProperties applicationProperties

    OutputThread(OutputQueue outputQueue, ApplicationContext applicationContext) {
        setName("Output_" + outputQueue.getName())
        this.outputQueue = outputQueue
        (1..outputQueue.normalThreadCount).each {
            SenderThread senderThread = new SenderThread(outputQueue, it)
            applicationContext.getAutowireCapableBeanFactory().autowireBean(senderThread)
            senderThreadRobin.add(senderThread)
            senderThread.start()
        }
    }

    abstract LinkedHashSet<OutputMessage> masterQuery(String subscriberName)

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
    void run() {
        while (true) {
            Set<OutputMessage> outputMessages = masterQuery(outputQueue.getName())
            if (outputMessages.size() > 0) {
                outputMessages.each { outputMessage ->
                    workerEnqueue(outputMessage)
                }
            }
            sleep(applicationProperties.outputThreadPollPeriodMilliseconds)
        }
    }

}
