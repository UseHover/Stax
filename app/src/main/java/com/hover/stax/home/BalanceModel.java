package com.hover.stax.home;

public class BalanceModel {
	private String channelName;
	private String actionId;
	private String encryptedPin;

public BalanceModel(String channelName, String actionId, String encryptedPin) {
	this.channelName = channelName;
	this.actionId = actionId;
	this.encryptedPin = encryptedPin;
}

public BalanceModel() {

}

public String getEncryptedPin() {
	return encryptedPin;
}

public void setEncryptedPin(String encryptedPin) {
	this.encryptedPin = encryptedPin;
}

public String getChannelName() {
	return channelName;
}

public void setChannelName(String channelName) {
	this.channelName = channelName;
}

public String getActionId() {
	return actionId;
}

public void setActionId(String actionId) {
	this.actionId = actionId;
}
}
