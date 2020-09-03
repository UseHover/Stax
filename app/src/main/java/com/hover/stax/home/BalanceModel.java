package com.hover.stax.home;

import com.hover.stax.channels.Channel;

public class BalanceModel {
	private String actionId;
	private Channel channel;
	private String balanceValue;
	private Long timeStamp;

	public BalanceModel(String actionId, Channel channel, String balanceValue, long timeStamp) {
		this.channel = channel;
		this.actionId = actionId;
		this.balanceValue = balanceValue;
		this.timeStamp =timeStamp;
	}

	public BalanceModel() {

	}

	public String getBalanceValue() {
		return balanceValue;
	}

	public void setBalanceValue(String balanceValue) {
		this.balanceValue = balanceValue;
	}

	public Long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
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
