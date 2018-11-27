package io.infinite.tpn.conf

class InputQueue {

    String name

    Long pollPeriodMilliseconds = 500

    OutputQueue[] outputQueues

}
