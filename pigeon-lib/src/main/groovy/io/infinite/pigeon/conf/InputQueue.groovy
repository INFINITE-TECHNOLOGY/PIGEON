package io.infinite.pigeon.conf

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
class InputQueue {

    String name
    Long pollPeriodMilliseconds = 500
    OutputQueue[] outputQueues
    Boolean enabled = true

}
