package com.hover.stax.home;

import com.hover.stax.models.StaxDate;

public class StaxTransactionModel {
	private String channelName, toTransactionType, amount, transactionUUIDId, actionId;
	private int channelId;
	private StaxDate staxDate;
	private boolean showDate;

	public StaxDate getStaxDate() {
		return staxDate;
	}

	void setStaxDate(StaxDate staxDate) {
		this.staxDate = staxDate;
	}

	public String getActionId() {
		return actionId;
	}

	void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public String getTransactionUUIDId() {
		return transactionUUIDId;
	}

	void setTransactionUUIDId(String transactionUUIDId) {
		this.transactionUUIDId = transactionUUIDId;
	}

	public int getChannelId() {
		return channelId;
	}

	void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public String getChannelName() {
		return channelName;
	}

	void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getToTransactionType() {
		return toTransactionType;
	}

	void setToTransactionType(String toTransactionType) {
		this.toTransactionType = toTransactionType;
	}

	public boolean isShowDate() {
		return showDate;
	}

	public void setShowDate(boolean showDate) {
		this.showDate = showDate;
	}

	public String getAmount() {
		return amount;
	}

	void setAmount(String amount) {
		this.amount = amount;
	}
}
