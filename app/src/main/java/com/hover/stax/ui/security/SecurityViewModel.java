package com.hover.stax.ui.security;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hover.stax.models.StaxServiceModel;
import com.hover.stax.database.ConvertRawDatabaseDataToModels;

import java.util.ArrayList;

public class SecurityViewModel extends ViewModel {
private MutableLiveData<ArrayList<StaxServiceModel>> services;

public SecurityViewModel() {
	services = new MutableLiveData<>();
	services.setValue(new ArrayList<>());
}

public void loadServices() {
	services.postValue(new ConvertRawDatabaseDataToModels().getServiceAccountsForDefault());
}

public void setNewDefaultAccount(int listIndex) {
	ArrayList<StaxServiceModel> servicesModelArrayList = services.getValue();
	if(servicesModelArrayList !=null && servicesModelArrayList.size() > 0) {
		StaxServiceModel staxService = servicesModelArrayList.get(listIndex);
		new ConvertRawDatabaseDataToModels().makeDefaultServiceAccount(staxService.getServiceId());
	}


}

public LiveData<ArrayList<StaxServiceModel>> getServicesForDefaultAccount() {
	return services;
}
}
