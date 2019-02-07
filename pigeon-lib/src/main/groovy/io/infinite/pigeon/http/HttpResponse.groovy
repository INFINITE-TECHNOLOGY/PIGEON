package io.infinite.pigeon.http

import groovy.transform.CompileStatic
import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true, includeSuper = true)
@CompileStatic
class HttpResponse extends HttpMessageAbstract {

    Integer status
    final Date receiveDate = new Date()

}
