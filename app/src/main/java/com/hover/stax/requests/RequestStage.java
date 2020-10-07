package com.hover.stax.requests;

enum RequestStage {
	RECIPIENT, AMOUNT, NOTE, REVIEW, REVIEW_DIRECT;

	private static RequestStage[] vals = values();

	public RequestStage next() {
		return vals[(this.ordinal() + 1) % vals.length];
	}

	public RequestStage prev() {
		return vals[(this.ordinal() - 1) % vals.length];
	}
}
