package com.hover.stax.transfers;

class CustomizedTransferSummarySettings {
	private String amountInput, recipientInput;
	private boolean isAmountClickable;
	public boolean isRecipientClickable = false, isActionRadioClickable = false;

	public String getAmountInput() {
		return amountInput;
	}

	public void setAmountInput(String amountInput) {
		this.amountInput = amountInput;
	}

	public String getRecipientInput() {
		return recipientInput;
	}

	public void setRecipientInput(String recipientInput) {
		this.recipientInput = recipientInput;
	}

	public boolean isAmountClickable() {
		return isAmountClickable;
	}

	public void setAmountClickable(boolean amountClickable) {
		isAmountClickable = amountClickable;
	}

}
