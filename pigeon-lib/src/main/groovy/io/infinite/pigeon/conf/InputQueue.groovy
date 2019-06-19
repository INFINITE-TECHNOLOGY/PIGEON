package io.infinite.pigeon.conf

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
class InputQueue {

    String name
    Long pollPeriodMilliseconds = 500
    Long recoveryTryPeriodMilliseconds = 60000
    OutputQueue[] outputQueues
    Boolean enabled = true

}
