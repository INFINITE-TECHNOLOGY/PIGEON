package io.infinite.pigeon.threads

import groovy.transform.ToString
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.pigeon.conf.OutputQueue
import io.infinite.pigeon.springdatarest.entities.OutputMessage
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import java.util.concurrent.LinkedBlockingQueue

@BlackBox(level = BlackBoxLevel.METHOD)
@ToString(includeNames = true, includeFields = true, includeSuper = true)
@Component
@Scope("prototype")
class OutputThreadNormal extends OutputThread {

    LinkedBlockingQueue<OutputMessage> outputMessages = new LinkedBlockingQueue<>()

    OutputThreadNormal(OutputQueue outputQueue) {
        super(outputQueue)
    }

    @Override
    @BlackBox(level = BlackBoxLevel.ERROR)
    void run() {
        while (true) {
            senderEnqueue(outputMessages.take())
        }
    }

}
