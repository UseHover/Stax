package com.hover.stax.adapters;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hover.stax.models.StaxServicesModel;
import com.hover.stax.repo.DataRepo;

import java.util.ArrayList;

public class ChooseServiceViewModel extends ViewModel {
private MutableLiveData<ArrayList<StaxServicesModel>> yourSimsDataAsList;
private MutableLiveData<ArrayList<StaxServicesModel>> inYourCountryDataAsList;
private MutableLiveData<ArrayList<StaxServicesModel>> allServicesDataAsList;

public ChooseServiceViewModel() {
	yourSimsDataAsList = new MutableLiveData<>();
	inYourCountryDataAsList = new MutableLiveData<>();
	allServicesDataAsList = new MutableLiveData<>();

	yourSimsDataAsList.setValue(new ArrayList<>());
	inYourCountryDataAsList.setValue(new ArrayList<>());
	allServicesDataAsList.setValue(new ArrayList<>());
}

public LiveData<ArrayList<StaxServicesModel>> loadServicesBasedOnSim() {
	return yourSimsDataAsList;
}
public LiveData<ArrayList<StaxServicesModel>> loadServicesBasedOnCountry() {
	return inYourCountryDataAsList;
}
public LiveData<ArrayList<StaxServicesModel>> loadAllServices() {
	return allServicesDataAsList;
}

public void getServicesBasedOnSim_liveData() {
	yourSimsDataAsList.postValue(new DataRepo().getServicesBasedOnSim());
}
public void getServicesBasedOnCountry_liveData() {
	inYourCountryDataAsList.postValue(new DataRepo().getServicesBasedOnCountry());
}

public void getAllServices_liveData() {
	allServicesDataAsList.postValue(new DataRepo().getAllServices());
}

}
