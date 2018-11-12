package io.infinite.tpn.threads

import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.tpn.conf.OutputQueue
import io.infinite.tpn.springdatarest.OutputMessage

import java.util.concurrent.LinkedBlockingQueue

class SenderThread extends Thread {

    OutputQueue outputQueue

    LinkedBlockingQueue<OutputMessage> sendingQueue = new LinkedBlockingQueue<>()

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    SenderThread(OutputQueue outputQueue) {
        setName(outputQueue.getUrl())
        this.outputQueue = outputQueue
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    void run() {

    }

}
