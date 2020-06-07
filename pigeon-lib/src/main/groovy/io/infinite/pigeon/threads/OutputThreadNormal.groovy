package io.infinite.pigeon.threads

import groovy.transform.ToString
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.pigeon.config.OutputQueue
import io.infinite.pigeon.entities.OutputMessage

import java.util.concurrent.LinkedBlockingQueue

@BlackBox(level = CarburetorLevel.METHOD)
@ToString(includeNames = true, includeFields = true, includeSuper = true)
class OutputThreadNormal extends OutputThread {

    LinkedBlockingQueue<OutputMessage> outputMessages = new LinkedBlockingQueue<>()

    OutputThreadNormal(OutputQueue outputQueue, InputThread inputThread) {
        super(outputQueue, inputThread)
    }

    @Override
    @BlackBox(level = CarburetorLevel.ERROR)
    void run() {
        while (true) {
            senderEnqueue(outputMessages.take())
        }
    }

}
