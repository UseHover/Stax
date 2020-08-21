package com.hover.stax.database;

import com.hover.stax.enums.Service_in_list_status;
import com.hover.stax.models.BetweenServicesDataModel;
import com.hover.stax.models.ChoosePinModel;
import com.hover.stax.models.StaxGetServiceAndActionModel;
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

public ArrayList<BetweenServicesDataModel> getBetweenServicesData() {
		ArrayList<BetweenServicesDataModel> betweenServicesList = new ArrayList<>();

	BetweenServicesDataModel betweenServicesDataModel0 = new BetweenServicesDataModel();
	betweenServicesDataModel0.setServiceName("Choose an account");

	ArrayList<StaxGetServiceAndActionModel> innerModelArrayList0 = new ArrayList<>();
	innerModelArrayList0.add(new StaxGetServiceAndActionModel("Choose an account", "asdadsa"));
	betweenServicesDataModel0.setStaxGetServiceAndActionModel(innerModelArrayList0);
	betweenServicesList.add(betweenServicesDataModel0);


		BetweenServicesDataModel betweenServicesDataModel1 = new BetweenServicesDataModel();

		betweenServicesDataModel1.setServiceName("Option 1");

		ArrayList<StaxGetServiceAndActionModel> innerModelArrayList = new ArrayList<>();
		innerModelArrayList.add(new StaxGetServiceAndActionModel("Option 2", "asdadsa"));
		innerModelArrayList.add(new StaxGetServiceAndActionModel("Option 3", "asdadsa"));
		innerModelArrayList.add(new StaxGetServiceAndActionModel("Option 4", "asdadsa"));
		innerModelArrayList.add(new StaxGetServiceAndActionModel("Option 5", "asdadsa"));
		betweenServicesDataModel1.setStaxGetServiceAndActionModel(innerModelArrayList);

	BetweenServicesDataModel betweenServicesDataModel2 = new BetweenServicesDataModel();
	betweenServicesDataModel2.setServiceName("Option 2");

	ArrayList<StaxGetServiceAndActionModel> innerModelArrayList2 = new ArrayList<>();
	innerModelArrayList2.add(new StaxGetServiceAndActionModel("Option 1", "asdadsa"));
	innerModelArrayList2.add(new StaxGetServiceAndActionModel("Option 3", "asdadsa"));
	innerModelArrayList2.add(new StaxGetServiceAndActionModel("Option 4", "asdadsa"));
	innerModelArrayList2.add(new StaxGetServiceAndActionModel("Option 5", "asdadsa"));
	betweenServicesDataModel2.setStaxGetServiceAndActionModel(innerModelArrayList2);


	BetweenServicesDataModel betweenServicesDataModel3 = new BetweenServicesDataModel();
	betweenServicesDataModel3.setServiceName("Option 3");

	ArrayList<StaxGetServiceAndActionModel> innerModelArrayList3 = new ArrayList<>();
	innerModelArrayList3.add(new StaxGetServiceAndActionModel("Option 1", "asdadsa"));
	innerModelArrayList3.add(new StaxGetServiceAndActionModel("Option 2", "asdadsa"));
	innerModelArrayList3.add(new StaxGetServiceAndActionModel("Option 4", "asdadsa"));
	innerModelArrayList3.add(new StaxGetServiceAndActionModel("Option 5", "asdadsa"));
	betweenServicesDataModel3.setStaxGetServiceAndActionModel(innerModelArrayList3);

	BetweenServicesDataModel betweenServicesDataModel4 = new BetweenServicesDataModel();
	betweenServicesDataModel4.setServiceName("Option 4");

	ArrayList<StaxGetServiceAndActionModel> innerModelArrayList4 = new ArrayList<>();
	innerModelArrayList4.add(new StaxGetServiceAndActionModel("Option 2", "asdadsa"));
	innerModelArrayList4.add(new StaxGetServiceAndActionModel("Option 3", "asdadsa"));
	innerModelArrayList4.add(new StaxGetServiceAndActionModel("Option 1", "asdadsa"));
	innerModelArrayList4.add(new StaxGetServiceAndActionModel("Option 5", "asdadsa"));
	betweenServicesDataModel4.setStaxGetServiceAndActionModel(innerModelArrayList4);

	BetweenServicesDataModel betweenServicesDataModel5 = new BetweenServicesDataModel();
	betweenServicesDataModel5.setServiceName("Option 5");

	ArrayList<StaxGetServiceAndActionModel> innerModelArrayList5 = new ArrayList<>();
	innerModelArrayList5.add(new StaxGetServiceAndActionModel("Option 2", "asdadsa"));
	innerModelArrayList5.add(new StaxGetServiceAndActionModel("Option 3", "asdadsa"));
	innerModelArrayList5.add(new StaxGetServiceAndActionModel("Option 4", "asdadsa"));
	innerModelArrayList5.add(new StaxGetServiceAndActionModel("Option 1", "asdadsa"));
	betweenServicesDataModel5.setStaxGetServiceAndActionModel(innerModelArrayList5);

		betweenServicesList.add(betweenServicesDataModel1);
	betweenServicesList.add(betweenServicesDataModel2);
	betweenServicesList.add(betweenServicesDataModel3);
	betweenServicesList.add(betweenServicesDataModel4);
	betweenServicesList.add(betweenServicesDataModel5);

	return betweenServicesList;

}


}
