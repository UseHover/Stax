package com.hover.stax.repo;

import com.hover.stax.adapters.ChooseServiceViewModel;
import com.hover.stax.enums.Service_in_list_status;
import com.hover.stax.models.StaxServicesModel;

import java.util.ArrayList;

public class DataRepo {
	public ArrayList<StaxServicesModel> getServicesBasedOnSim() {
		ArrayList<StaxServicesModel> services = new ArrayList<>();

		services.add(new StaxServicesModel("123", "Safaricom", null, false));
		services.add(new StaxServicesModel("143", "MTN MoMo", null, false));

		return services;
	}

public ArrayList<StaxServicesModel> getServicesBasedOnCountry() {
	ArrayList<StaxServicesModel> services = new ArrayList<>();

	services.add(new StaxServicesModel("123", "Safaricom", null, true));
	services.add(new StaxServicesModel("143", "MTN MoMo", null, true));
	services.add(new StaxServicesModel("123", "Safaricom", null, false));
	services.add(new StaxServicesModel("143", "MTN MoMo", null,false));
	services.add(new StaxServicesModel("123", "Safaricom", null,false));
	services.add(new StaxServicesModel("143", "MTN MoMo", null, false));
	services.add(new StaxServicesModel("123", "Safaricom", null, false));

	return services;
}

public ArrayList<StaxServicesModel> getAllServices() {
	ArrayList<StaxServicesModel> services = new ArrayList<>();
	services.add(new StaxServicesModel("123", "Safaricom", null,false));
	services.add(new StaxServicesModel("143", "MTN MoMo", null, false));
	services.add(new StaxServicesModel("123", "Safaricom", null, false));
	services.add(new StaxServicesModel("123", "Safaricom", null,false));
	services.add(new StaxServicesModel("143", "MTN MoMo", null, false));
	services.add(new StaxServicesModel("123", "Safaricom", null, false));
	services.add(new StaxServicesModel("123", "Safaricom", null, true));
	services.add(new StaxServicesModel("143", "MTN MoMo", null, true));
	services.add(new StaxServicesModel("123", "Safaricom", null, false));
	services.add(new StaxServicesModel("143", "MTN MoMo", null,false));
	services.add(new StaxServicesModel("123", "Safaricom", null,false));
	services.add(new StaxServicesModel("143", "MTN MoMo", null, false));
	services.add(new StaxServicesModel("123", "Safaricom", null, false));

	return services;
}

public boolean addServiceToUserCatalogue(Service_in_list_status newStatus, String serviceId) {
		return true;
}

public ArrayList<StaxServicesModel> getServiceAccountsForDefault() {
		//Return items with the default service in position 0.
	ArrayList<StaxServicesModel> services = new ArrayList<>();
	services.add(new StaxServicesModel("123", "Safaricom"));
	services.add(new StaxServicesModel("123", "MTN MoMo"));
	services.add(new StaxServicesModel("123", "Others 2"));
	services.add(new StaxServicesModel("123", "Others 3"));
	services.add(new StaxServicesModel("123", "Others 4"));
	services.add(new StaxServicesModel("123", "Others 5"));
	services.add(new StaxServicesModel("123", "Others 6"));

	return services;
}

public void makeDefaultServiceAccount(String serviceId) {

}

public String getSimCountry() {
		return "Kenya";
}


}
