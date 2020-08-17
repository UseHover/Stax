package com.hover.stax.models;

import android.graphics.Bitmap;

public class ChoosePinModel {
	private String label, currentValue, serviceId;
	private Bitmap serviceLogo;
	private boolean hasValue;

public ChoosePinModel() {
}

public ChoosePinModel(String label, String currentValue, String serviceId, Bitmap serviceLogo, boolean hasValue) {
	this.label = label;
	this.currentValue = currentValue;
	this.serviceId = serviceId;
	this.serviceLogo = serviceLogo;
	this.hasValue = hasValue;
}

public boolean isHasValue() {
	return hasValue;
}

public void setHasValue(boolean hasValue) {
	this.hasValue = hasValue;
}

public String getLabel() {
	return label;
}

public void setLabel(String label) {
	this.label = label;
}

public String getCurrentValue() {
	return currentValue;
}

public void setCurrentValue(String currentValue) {
	this.currentValue = currentValue;
}

public String getServiceId() {
	return serviceId;
}

public void setServiceId(String serviceId) {
	this.serviceId = serviceId;
}

public Bitmap getServiceLogo() {
	return serviceLogo;
}

public void setServiceLogo(Bitmap serviceLogo) {
	this.serviceLogo = serviceLogo;
}
}
