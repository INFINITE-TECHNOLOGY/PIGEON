package io.infinite.tpn

enum MessageStatus {

    NEW("new"),
    WAITING_FOR_MASTER_AFTER_RESTART("waiting_for_master_after_restart"),
    SPLIT("split"),
    DUPLICATE("duplicate"),
    WAITING_FOR_MASTER("waiting_for_master"),
    WAITING_FOR_WORKER("waiting_for_worker");

    private final String messageStatus

    MessageStatus(String iMessageStatus) {
        messageStatus = iMessageStatus
    }

    String value() {
        return messageStatus
    }

}