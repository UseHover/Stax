package com.hover.stax.home;

public class BalanceModel {
	private String channelName;
	private String actionId;

BalanceModel(String channelName, String actionId) {
	this.channelName = channelName;
	this.actionId = actionId;
}

public BalanceModel() {

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
