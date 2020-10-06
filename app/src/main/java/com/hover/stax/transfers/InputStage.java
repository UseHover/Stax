package com.hover.stax.transfers;

enum InputStage {
	AMOUNT, FROM_ACCOUNT, TO_NETWORK, TO_NUMBER, REASON, REVIEW, REVIEW_DIRECT;

	private static InputStage[] vals = values();

	public InputStage next() {
		return vals[(this.ordinal() + 1) % vals.length];
	}

	public InputStage prev() {
		return vals[(this.ordinal() - 1) % vals.length];
	}
}
