package io.infinite.pigeon.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
@JsonIgnoreProperties(ignoreUnknown = true)
class PigeonConf {

    InputQueue[] inputQueues

}
