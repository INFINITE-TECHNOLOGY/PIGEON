package io.infinite.pigeon.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
@JsonIgnoreProperties(ignoreUnknown = true)
class OutputQueue {

    /**
     * OutputQueue Name, must be unique within all Queues.
     */
    String name
    String url
    Integer maxRetryCount = 0
    Integer resendIntervalSeconds = 86400 /*1 day*/
    Integer normalThreadCount = 4
    Integer retryThreadCount = 0
    String conversionModuleName
    String senderClassName = "io.infinite.pigeon.http.SenderDefaultHttps"
    Long pollPeriodMillisecondsRetry = 60000
    Map<String, Object> httpProperties = [:]
    Map<String, Object> extensions = [:]
    Boolean enabled = true

}
