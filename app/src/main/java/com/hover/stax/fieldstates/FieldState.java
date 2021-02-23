package com.hover.stax.fieldstates;

public class FieldState {
	private  String message;
	private FieldStates fieldStates;

	public FieldState(String message, FieldStates fieldStates) {
		this.message = message;
		this.fieldStates = fieldStates;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public FieldStates getFieldStates() {
		return fieldStates;
	}

	public void setFieldStates(FieldStates fieldStates) {
		this.fieldStates = fieldStates;
	}
}
