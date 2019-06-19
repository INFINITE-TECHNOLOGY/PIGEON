package io.infinite.pigeon.conf

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
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
    String senderClassName
    Long pollPeriodMillisecondsRetry = 60000
    Long recoveryTryPeriodMillisecondsRetry = 60000
    Map<String, Object> httpProperties = new HashMap<>()
    Map<String, Object> extensions = new HashMap<>()
    Boolean enabled = true

}
