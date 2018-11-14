package io.infinite.tpn.http

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true, includeSuper = true)
class HttpResponse extends HttpMessageAbstract {

    Integer status
    Date receiveDate

}
