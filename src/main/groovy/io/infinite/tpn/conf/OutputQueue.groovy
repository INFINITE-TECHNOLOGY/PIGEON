package io.infinite.tpn.conf

class OutputQueue {

    /**
     * OutputQueue Name, must be unique within all Queues.
     */
    String name
    String url
    String awsServiceName
    String awsRegion
    String awsAccessKey
    String awsSecretKey
    String awsResourceName
    Integer maxRetryCount = 0
    Integer resendIntervalSeconds = 86400
    Integer normalThreadCount = 4
    Integer retryThreadCount = 0
    String conversionModuleName
    String senderClassName

}
