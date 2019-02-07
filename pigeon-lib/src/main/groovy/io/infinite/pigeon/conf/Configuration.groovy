package io.infinite.pigeon.conf

import groovy.transform.CompileStatic
import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
@CompileStatic
class Configuration {

    InputQueue[] inputQueues

}
