package io.infinite.pigeon.threads


import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.conf.OutputQueue
import io.infinite.pigeon.springdatarest.entities.OutputMessage
import org.springframework.context.ApplicationContext

import java.util.concurrent.LinkedBlockingQueue

@BlackBox
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
