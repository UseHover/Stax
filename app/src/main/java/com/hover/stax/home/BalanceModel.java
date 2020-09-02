package com.hover.stax.home;

import com.hover.stax.channels.Channel;

public class BalanceModel {
	private String actionId;
	private Channel channel;

public BalanceModel(String actionId, Channel channel) {
	this.channel = channel;
	this.actionId = actionId;
}

public BalanceModel() {

}

public Channel getChannel() {
	return channel;
}

public void setChannel(Channel channel) {
	this.channel = channel;
}

public String getActionId() {
	return actionId;
}

public void setActionId(String actionId) {
	this.actionId = actionId;
}
}
