package io.infinite.tpn

enum MessageStatus {

    NEW("new"),
    SPLITTED("splitted"),
    READY_FOR_SENDING("ready_for_sending");

    private final String messageStatus

    MessageStatus(String iMessageStatus) {
        messageStatus = iMessageStatus
    }

    String value() {
        return messageStatus
    }

}