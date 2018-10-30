package io.infinite.tpn.threads

import io.infinite.tpn.conf.Subscriber

class WorkerRetryThread extends WorkerThread {

    WorkerRetryThread(Subscriber subscriber) {
        super(subscriber)
    }

}
