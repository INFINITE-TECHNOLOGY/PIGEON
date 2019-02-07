package io.infinite.pigeon.http

import groovy.transform.CompileStatic
import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
@CompileStatic
class HttpMessageAbstract {

    HashMap<String, String> headers = new HashMap<>()
    String body

}
