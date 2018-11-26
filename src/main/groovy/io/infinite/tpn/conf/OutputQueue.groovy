package io.infinite.tpn.conf

class OutputQueue {

    /**
     * OutputQueue Name, must be unique within all Queues.
     */
    String name
    String url
    Integer maxRetryCount = 0
    Integer resendIntervalSeconds = 86400
    Integer normalThreadCount = 4
    Integer retryThreadCount = 0
    String conversionModuleName
    String senderClassName
    Map<String, String> httpProperties = new HashMap<>()
    Map<String, String> extensions = new HashMap<>()

}
