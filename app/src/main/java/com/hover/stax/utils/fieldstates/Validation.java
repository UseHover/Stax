package com.hover.stax.utils.fieldstates;

public enum Validation {
	SOFT, //validates only for success states. Error, warning and info are ignored. Useful to show filled fields.
	HARD //validates for all states.
}
