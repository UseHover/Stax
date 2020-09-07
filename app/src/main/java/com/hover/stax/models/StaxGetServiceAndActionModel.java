package com.hover.stax.models;

public class StaxGetServiceAndActionModel {
	private String serviceName, actionIdLinkingBothServices;

public StaxGetServiceAndActionModel(String serviceName, String actionIdLinkingBothServices) {
	this.serviceName = serviceName;
	this.actionIdLinkingBothServices = actionIdLinkingBothServices;
}

public String getServiceName() {
	return serviceName;
}

public void setServiceName(String serviceName) {
	this.serviceName = serviceName;
}

public String getActionIdLinkingBothServices() {
	return actionIdLinkingBothServices;
}

public void setActionIdLinkingBothServices(String actionIdLinkingBothServices) {
	this.actionIdLinkingBothServices = actionIdLinkingBothServices;
}
}
