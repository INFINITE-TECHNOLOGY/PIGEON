package io.infinite.pigeon.conf

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
@JsonIgnoreProperties(ignoreUnknown = true)
class PigeonConf {

    List<InputQueue> inputQueues = []

}
