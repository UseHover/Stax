package com.hover.stax.requests;

import com.hover.stax.utils.StagedViewModel;

enum RequestStage implements StagedViewModel.StagedEnum {
	AMOUNT, REQUESTEE, REQUESTER, NOTE, REVIEW, REVIEW_DIRECT;

	private static RequestStage[] vals = values();

	public RequestStage next() {
		return vals[(this.ordinal() + 1) % vals.length];
	}

	public RequestStage prev() {
		return vals[(this.ordinal() - 1) % vals.length];
	}

	public int compare(StagedViewModel.StagedEnum e) {
		return compareTo((RequestStage) e);
	}
}
