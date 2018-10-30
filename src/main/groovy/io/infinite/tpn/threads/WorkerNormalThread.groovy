package io.infinite.tpn.threads

import io.infinite.tpn.conf.Subscriber

class WorkerNormalThread extends WorkerThread {

    WorkerNormalThread(Subscriber subscriber) {
        super(subscriber)
    }

}
