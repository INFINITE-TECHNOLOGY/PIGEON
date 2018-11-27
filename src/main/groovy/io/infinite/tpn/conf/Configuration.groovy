package io.infinite.tpn.conf

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
class Configuration {

    InputQueue[] inputQueues

}
