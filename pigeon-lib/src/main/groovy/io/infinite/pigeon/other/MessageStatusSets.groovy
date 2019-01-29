package io.infinite.pigeon.other

enum MessageStatusSets {

    INPUT_NEW_MESSAGE_STATUSES([MessageStatuses.NEW.value(), MessageStatuses.NEW2.value()] as String[]),
    OUTPUT_NORMAL_MESSAGE_STATUSES([MessageStatuses.NEW.value()] as String[]),
    OUTPUT_RETRY_MESSAGE_STATUSES([MessageStatuses.RENEWED.value(), MessageStatuses.FAILED_INVALID_RESPONSE.value(), MessageStatuses.FAILED_NO_CONNECTION.value()] as String[]);

    private final String[] messageStatuses

    MessageStatusSets(String[] iMessageStatuses) {
        messageStatuses = iMessageStatuses
    }

    List<String> value() {
        return messageStatuses
    }

}