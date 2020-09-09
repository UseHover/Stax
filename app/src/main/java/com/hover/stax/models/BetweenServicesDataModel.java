package com.hover.stax.models;

import java.util.ArrayList;

public class BetweenServicesDataModel {
	private String serviceName;
	private ArrayList<StaxGetServiceAndActionModel> staxGetServiceAndActionModel;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public ArrayList<StaxGetServiceAndActionModel> getStaxGetServiceAndActionModel() {
		return staxGetServiceAndActionModel;
	}

	public void setStaxGetServiceAndActionModel(ArrayList<StaxGetServiceAndActionModel> staxGetServiceAndActionModel) {
		this.staxGetServiceAndActionModel = staxGetServiceAndActionModel;
	}
}
