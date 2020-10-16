package com.hover.stax.transfers;

import com.hover.stax.utils.StagedViewModel;

enum TransferStage implements StagedViewModel.StagedEnum {
	AMOUNT, FROM_ACCOUNT, TO_NETWORK, RECIPIENT, NOTE, REVIEW, REVIEW_DIRECT;

	private static TransferStage[] vals = values();

	public TransferStage next() {
		return vals[(this.ordinal() + 1) % vals.length];
	}

	public TransferStage prev() {
		return vals[(this.ordinal() - 1) % vals.length];
	}

	public int compare(StagedViewModel.StagedEnum e) {
		return compareTo((TransferStage) e);
	}
}
