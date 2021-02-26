package com.hover.stax.utils.fieldstates;

public class FieldState {
	private  String message;
	private FieldStateType fieldStateType;

	public FieldState(FieldStateType fieldStateType, String message) {
		this.message = message;
		this.fieldStateType = fieldStateType;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public FieldStateType getFieldStateType() {
		return fieldStateType;
	}

	public void setFieldStateType(FieldStateType fieldStateType) {
		this.fieldStateType = fieldStateType;
	}
}
