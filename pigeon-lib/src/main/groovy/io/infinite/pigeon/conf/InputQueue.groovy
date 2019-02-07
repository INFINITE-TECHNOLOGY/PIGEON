package io.infinite.pigeon.conf

import groovy.transform.CompileStatic
import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
@CompileStatic
class InputQueue {

    String name
    Long pollPeriodMilliseconds = 500
    OutputQueue[] outputQueues
    Boolean enabled = true

}
