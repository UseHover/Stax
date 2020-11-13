package com.hover.stax.transfers;

class CustomizedTransferSummarySettings {
	private String amountInput, recipientInput;
	private boolean isAmountClickable, isRecipientClickable, isActionRadioClickable;

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

	public boolean isRecipientClickable() {
		return isRecipientClickable;
	}

	public void setRecipientClickable(boolean recipientClickable) {
		isRecipientClickable = recipientClickable;
	}

	public boolean isActionRadioClickable() {
		return isActionRadioClickable;
	}

	public void setActionRadioClickable(boolean actionRadioClickable) {
		isActionRadioClickable = actionRadioClickable;
	}
}
