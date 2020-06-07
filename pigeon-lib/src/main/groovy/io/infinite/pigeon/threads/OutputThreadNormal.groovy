package io.infinite.pigeon.threads


import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.pigeon.config.OutputQueue
import io.infinite.pigeon.entities.OutputMessage
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
            senderEnqueue(outputMessages.take())
        }
    }

}
