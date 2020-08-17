package com.hover.stax.database;

import com.hover.stax.enums.Service_in_list_status;
import com.hover.stax.models.ChoosePinModel;
import com.hover.stax.models.StaxServiceModel;

import java.util.ArrayList;

public class ConvertRawDatabaseDataToModels {
	public ArrayList<StaxServiceModel> getServicesBasedOnSim() {
		ArrayList<StaxServiceModel> services = new ArrayList<>();

		services.add(new StaxServiceModel("123", "Safaricom", null, false));
		services.add(new StaxServiceModel("143", "MTN MoMo", null, false));

		return services;
	}

public ArrayList<StaxServiceModel> getServicesBasedOnCountry() {
	ArrayList<StaxServiceModel> services = new ArrayList<>();

	services.add(new StaxServiceModel("123", "Safaricom", null, true));
	services.add(new StaxServiceModel("143", "MTN MoMo", null, true));
	services.add(new StaxServiceModel("123", "Safaricom", null, false));
	services.add(new StaxServiceModel("143", "MTN MoMo", null,false));
	services.add(new StaxServiceModel("123", "Safaricom", null,false));
	services.add(new StaxServiceModel("143", "MTN MoMo", null, false));
	services.add(new StaxServiceModel("123", "Safaricom", null, false));

	return services;
}

public ArrayList<StaxServiceModel> getAllServices() {
	ArrayList<StaxServiceModel> services = new ArrayList<>();
	services.add(new StaxServiceModel("123", "Safaricom", null,false));
	services.add(new StaxServiceModel("143", "MTN MoMo", null, false));
	services.add(new StaxServiceModel("123", "Safaricom", null, false));
	services.add(new StaxServiceModel("123", "Safaricom", null,false));
	services.add(new StaxServiceModel("143", "MTN MoMo", null, false));
	services.add(new StaxServiceModel("123", "Safaricom", null, false));
	services.add(new StaxServiceModel("123", "Safaricom", null, true));
	services.add(new StaxServiceModel("143", "MTN MoMo", null, true));
	services.add(new StaxServiceModel("123", "Safaricom", null, false));
	services.add(new StaxServiceModel("143", "MTN MoMo", null,false));
	services.add(new StaxServiceModel("123", "Safaricom", null,false));
	services.add(new StaxServiceModel("143", "MTN MoMo", null, false));
	services.add(new StaxServiceModel("123", "Safaricom", null, false));

	return services;
}

public boolean addServiceToUserCatalogue(Service_in_list_status newStatus, String serviceId) {
		return true;
}

public ArrayList<StaxServiceModel> getServiceAccountsForDefault() {
		//Return items with the default service in position 0.
	ArrayList<StaxServiceModel> services = new ArrayList<>();
	services.add(new StaxServiceModel("123", "Safaricom"));
	services.add(new StaxServiceModel("123", "MTN MoMo"));
	services.add(new StaxServiceModel("123", "Others 2"));
	services.add(new StaxServiceModel("123", "Others 3"));
	services.add(new StaxServiceModel("123", "Others 4"));
	services.add(new StaxServiceModel("123", "Others 5"));
	services.add(new StaxServiceModel("123", "Others 6"));

	return services;
}

public void makeDefaultServiceAccount(String serviceId) {

}

public String getSimCountry() {
		return "Kenya";
}

public void saveUserServicePin(String serviceId, String newValue) {

}

public ArrayList<ChoosePinModel> loadServicePins() {
		ArrayList<ChoosePinModel> choosePinModelArrayList = new ArrayList<>();
		choosePinModelArrayList.add(new ChoosePinModel("Safaricom", "1232", "ada", null, true));
		choosePinModelArrayList.add(new ChoosePinModel("MTN MoMo", "1232", "ada", null, false));
		choosePinModelArrayList.add(new ChoosePinModel("Paga", "1232", "ada", null, false));
		choosePinModelArrayList.add(new ChoosePinModel("Airtel", "1232", "ada", null, true));
		choosePinModelArrayList.add(new ChoosePinModel("Globacom", "1232", "ada", null, true));
		return choosePinModelArrayList;
}


}
