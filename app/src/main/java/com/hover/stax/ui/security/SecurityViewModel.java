package com.hover.stax.ui.security;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hover.stax.models.StaxServicesModel;
import com.hover.stax.repo.DataRepo;

import java.util.ArrayList;

public class SecurityViewModel extends ViewModel {
private MutableLiveData<ArrayList<StaxServicesModel>> services;

public SecurityViewModel() {
	services = new MutableLiveData<>();
	services.setValue(new ArrayList<>());
}

public void loadServices() {
	services.postValue(new DataRepo().getServiceAccountsForDefault());
}

public void setNewDefaultAccount(int listIndex) {
	ArrayList<StaxServicesModel> servicesModelArrayList = services.getValue();
	if(servicesModelArrayList !=null && servicesModelArrayList.size() > 0) {
		StaxServicesModel staxService = servicesModelArrayList.get(listIndex);
		new DataRepo().makeDefaultServiceAccount(staxService.getServiceId());
	}


}

public LiveData<ArrayList<StaxServicesModel>> getServicesForDefaultAccount() {
	return services;
}
}
