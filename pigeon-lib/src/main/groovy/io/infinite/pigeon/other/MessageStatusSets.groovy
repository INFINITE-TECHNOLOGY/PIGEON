package io.infinite.pigeon.other

enum MessageStatusSets {

    NO_RETRY_MESSAGE_STATUSES([MessageStatuses.NEW.value(), MessageStatuses.RENEWED.value()] as String[]),
    RETRY_MESSAGE_STATUSES([MessageStatuses.FAILED_INVALID_RESPONSE.value(), MessageStatuses.FAILED_NO_CONNECTION.value()] as String[]);

    private final String[] messageStatuses

    MessageStatusSets(String[] iMessageStatuses) {
        messageStatuses = iMessageStatuses
    }

    List<String> value() {
        return messageStatuses
    }

}