package io.infinite.pigeon.other

enum MessageStatuses {

    NEW("new"),
    NEW2("NEW"),
    DELIVERED("delivered"),
    FAILED_NO_CONNECTION("no_connection"),
    FAILED_RESPONSE("failed_response"),
    EXCEPTION("error"),
    WAITING("waiting"),
    DUPLICATE("duplicate"),
    RENEWED("renewed"),
    SENDING("sending"),
    SPLIT("split"),
    ENQUEUED("enqueued"),

    private final String messageStatus

    MessageStatuses(String iMessageStatus) {
        messageStatus = iMessageStatus
    }

    String value() {
        return messageStatus
    }

}