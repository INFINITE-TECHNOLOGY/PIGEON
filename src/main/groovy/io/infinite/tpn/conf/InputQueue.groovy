package io.infinite.tpn.conf

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
class InputQueue {

    String name
    Long pollPeriodMilliseconds = 500
    OutputQueue[] outputQueues

}
