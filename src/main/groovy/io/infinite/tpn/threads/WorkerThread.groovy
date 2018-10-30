package io.infinite.tpn.threads

import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.tpn.conf.Subscriber
import io.infinite.tpn.springdatarest.DestinationMessage

import java.util.concurrent.LinkedBlockingQueue

class WorkerThread extends Thread {

    Subscriber subscriber

    LinkedBlockingQueue<DestinationMessage> destinationMessageQueue = new LinkedBlockingQueue<>()

    WorkerThread(Subscriber subscriber) {
        this.subscriber = subscriber
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    void run() {

    }

}
