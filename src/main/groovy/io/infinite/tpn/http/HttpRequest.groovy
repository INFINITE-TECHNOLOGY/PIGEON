package io.infinite.tpn.http

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true, includeSuper = true)
class HttpRequest extends HttpMessageAbstract {

    String method
    String url
    String requestStatus
    String exceptionString
    final Date sendDate = new Date()
    Map<String, String> httpProperties = new HashMap<>()
    Map<String, String> extensions = new HashMap<>()

}
