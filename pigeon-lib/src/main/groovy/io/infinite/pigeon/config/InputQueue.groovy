package io.infinite.pigeon.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
@JsonIgnoreProperties(ignoreUnknown = true)
class InputQueue {

    String name
    Long pollPeriodMilliseconds = 500
    List<OutputQueue> outputQueues = []
    Boolean enabled = true
    Boolean enableScanDB = true

}
