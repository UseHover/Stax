package com.hover.stax.home.detailsPages.transaction;

public class TransactionDetailsMessagesModel {
    private String enteredValue, messageContent;

    public TransactionDetailsMessagesModel(String enteredValue, String messageContent) {
        this.enteredValue = enteredValue;
        this.messageContent = messageContent;
    }

    String getEnteredValue() {
        return enteredValue;
    }

    String getMessageContent() {
        return messageContent;
    }
}
