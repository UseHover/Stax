package com.hover.stax.ui.chooseService.choose;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hover.stax.models.StaxServiceModel;
import com.hover.stax.database.ConvertRawDatabaseDataToModels;

import java.util.ArrayList;

public class ChooseServiceViewModel extends ViewModel {
	private MutableLiveData<ArrayList<StaxServiceModel>> yourSimsDataAsList;
	private MutableLiveData<ArrayList<StaxServiceModel>> inYourCountryDataAsList;
	private MutableLiveData<ArrayList<StaxServiceModel>> allServicesDataAsList;

	public ChooseServiceViewModel() {
		yourSimsDataAsList = new MutableLiveData<>();
		inYourCountryDataAsList = new MutableLiveData<>();
		allServicesDataAsList = new MutableLiveData<>();

		yourSimsDataAsList.setValue(new ArrayList<>());
		inYourCountryDataAsList.setValue(new ArrayList<>());
		allServicesDataAsList.setValue(new ArrayList<>());
	}

	public LiveData<ArrayList<StaxServiceModel>> loadServicesBasedOnSim() {
		return yourSimsDataAsList;
	}
	public LiveData<ArrayList<StaxServiceModel>> loadServicesBasedOnCountry() {
		return inYourCountryDataAsList;
	}
	public LiveData<ArrayList<StaxServiceModel>> loadAllServices() {
		return allServicesDataAsList;
	}

	public void getServicesBasedOnSim_liveData() {
		yourSimsDataAsList.postValue(new ConvertRawDatabaseDataToModels().getServicesBasedOnSim());
	}
	public void getServicesBasedOnCountry_liveData() {
		inYourCountryDataAsList.postValue(new ConvertRawDatabaseDataToModels().getServicesBasedOnCountry());
	}

	public void getAllServices_liveData() {
		allServicesDataAsList.postValue(new ConvertRawDatabaseDataToModels().getAllServices());
	}
}
