package io.infinite.tpn

enum MessageStatusSets {

    NORMAL_MESSAGE_STATUSES([MessageStatus.WAITING_FOR_MASTER.value(), MessageStatus.WAITING_FOR_MASTER_AFTER_RESTART.value()] as String[]);

    private final String[] messageStatuses

    MessageStatusSets(String[] iMessageStatuses) {
        messageStatuses = iMessageStatuses
    }

    String value() {
        return messageStatuses
    }

}