package io.infinite.pigeon.other

enum MessageStatusSets {

    INPUT_RENEW_MESSAGE_STATUSES([MessageStatuses.NEW.value(), MessageStatuses.NEW2.value()] as String[]),
    OUTPUT_RENEW_MESSAGE_STATUSES([MessageStatuses.NEW.value(), MessageStatuses.WAITING.value()] as String[]),

    INPUT_NEW_MESSAGE_STATUSES([MessageStatuses.NEW.value(), MessageStatuses.NEW2.value(), MessageStatuses.RENEWED.value()] as String[]),
    OUTPUT_NORMAL_MESSAGE_STATUSES([MessageStatuses.NEW.value()] as String[]),
    OUTPUT_RETRY_MESSAGE_STATUSES([MessageStatuses.RENEWED.value(), MessageStatuses.FAILED_NO_CONNECTION.value()] as String[]);

    private final String[] messageStatuses

    MessageStatusSets(String[] iMessageStatuses) {
        messageStatuses = iMessageStatuses
    }

    List<String> value() {
        return messageStatuses as List<String>
    }

}