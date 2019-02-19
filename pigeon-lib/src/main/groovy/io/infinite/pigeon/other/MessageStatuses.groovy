package io.infinite.pigeon.other

enum MessageStatuses {

    NEW("new"),
    NEW2("NEW"),
    DELIVERED("delivered"),
    FAILED_NO_CONNECTION("no_connection"),
    FAILED_INVALID_RESPONSE("invalid_response"),
    FAILED_INVALID_REQUEST("invalid_request"),
    FAILED_RESPONSE("failed_response"),
    EXCEPTION("error"),
    WAITING("waiting"),
    DUPLICATE("duplicate"),
    RENEWED("renewed"),
    SENDING("sending"),
    OBSOLETE("obsolete"),
    SPLIT("split")

    private final String messageStatus

    MessageStatuses(String iMessageStatus) {
        messageStatus = iMessageStatus
    }

    String value() {
        return messageStatus
    }

}