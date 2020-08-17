package com.hover.stax.models;

import android.graphics.Bitmap;

public class StaxServiceModel {
private String serviceId, serviceName;
private Bitmap serviceLogo;
private Boolean isAdded;

public StaxServiceModel(String serviceId, String serviceName, Bitmap serviceLogo, boolean isAdded) {
	this.serviceId = serviceId;
	this.serviceName = serviceName;
	this.serviceLogo = serviceLogo;
	this.isAdded = isAdded;
}

public StaxServiceModel(String serviceId, String serviceName) {
	this.serviceId = serviceId;
	this.serviceName = serviceName;
}

public Boolean getAdded() {
	return isAdded;
}

public void setAdded(Boolean added) {
	isAdded = added;
}

public String getServiceId() {
	return serviceId;
}

public void setServiceId(String serviceId) {
	this.serviceId = serviceId;
}

public String getServiceName() {
	return serviceName;
}

public void setServiceName(String serviceName) {
	this.serviceName = serviceName;
}

public Bitmap getServiceLogo() {
	return serviceLogo;
}

public void setServiceLogo(Bitmap serviceLogo) {
	this.serviceLogo = serviceLogo;
}
}
