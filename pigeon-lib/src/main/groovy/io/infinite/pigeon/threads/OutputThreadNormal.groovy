package io.infinite.pigeon.threads

import groovy.transform.CompileStatic
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.conf.OutputQueue
import io.infinite.pigeon.springdatarest.OutputMessage
import org.springframework.context.ApplicationContext

import java.util.concurrent.LinkedBlockingQueue

@BlackBox
@CompileStatic
class OutputThreadNormal extends OutputThread {

    LinkedBlockingQueue<OutputMessage> messages = new LinkedBlockingQueue<>()

    OutputThreadNormal(OutputQueue outputQueue, InputThread inputThread, ApplicationContext applicationContext) {
        super(outputQueue, inputThread, applicationContext)
    }

    @Override
    void run() {
        while (true) {
            while (!messages.isEmpty()) {
                senderEnqueue(messages.poll())
            }
            synchronized (this) {
                this.wait()
            }
        }
    }



}
