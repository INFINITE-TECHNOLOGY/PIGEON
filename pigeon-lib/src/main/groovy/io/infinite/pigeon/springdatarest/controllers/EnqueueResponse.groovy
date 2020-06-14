package io.infinite.pigeon.springdatarest.controllers

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
class EnqueueResponse {

    String result
    String inputMessageUrl
    String readableHttpLogsUrl

}
