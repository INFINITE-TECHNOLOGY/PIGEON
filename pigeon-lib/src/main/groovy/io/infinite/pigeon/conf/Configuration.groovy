package io.infinite.pigeon.conf

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
class Configuration {

    InputQueue[] inputQueues

}
