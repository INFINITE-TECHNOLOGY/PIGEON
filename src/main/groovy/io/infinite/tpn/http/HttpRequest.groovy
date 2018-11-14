package io.infinite.tpn.http

import groovy.transform.ToString

import javax.persistence.Lob

@ToString(includeNames = true, includeFields = true, includeSuper = true)
class HttpRequest extends HttpMessageAbstract {

    String method
    String url
    String requestStatus
    @Lob
    String exceptionString
    Date sendDate

}
