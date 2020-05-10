package io.infinite.pigeon.mvc.controllers

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
class EnqueueResponse {

    String result
    String inputMessageUrl
    String readableHttpLogsUrl

}
