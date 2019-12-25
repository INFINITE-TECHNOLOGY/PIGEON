package io.infinite.pigeon.threads


import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.pigeon.conf.OutputQueue
import io.infinite.pigeon.springdatarest.entities.OutputMessage
import io.infinite.supplies.ast.exceptions.ExceptionUtils
import org.springframework.context.ApplicationContext

import java.util.concurrent.LinkedBlockingQueue

@BlackBox
class OutputThreadNormal extends OutputThread {

    LinkedBlockingQueue<OutputMessage> outputMessages = new LinkedBlockingQueue<>()

    OutputThreadNormal(OutputQueue outputQueue, InputThread inputThread, ApplicationContext applicationContext) {
        super(outputQueue, inputThread, applicationContext)
    }

    @Override
    @BlackBox(level = CarburetorLevel.ERROR)
    void run() {
        while (true) {
            while (!outputMessages.isEmpty()) {
                try {
                    senderEnqueue(outputMessages.poll())
                }  catch (Exception e) {
                    println("Output thread exception.")
                    println(new ExceptionUtils().stacktrace(e))
                    log.error("Output thread exception.", e)
                }
            }
            synchronized (this) {
                this.wait()
            }
        }
    }

}
