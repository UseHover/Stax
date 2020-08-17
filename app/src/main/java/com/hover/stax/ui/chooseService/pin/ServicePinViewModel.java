package com.hover.stax.ui.chooseService.pin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hover.stax.database.ConvertRawDatabaseDataToModels;
import com.hover.stax.models.ChoosePinModel;
import com.hover.stax.models.StaxServiceModel;

import java.util.ArrayList;

public class ServicePinViewModel extends ViewModel {
private MutableLiveData<ArrayList<ChoosePinModel>> chooseModelList;

public ServicePinViewModel() {
	chooseModelList = new MutableLiveData<>();
}

public void getServicePins() {
chooseModelList.postValue(new ConvertRawDatabaseDataToModels().loadServicePins());
}

public LiveData<ArrayList<ChoosePinModel>> loadServicePins() {
	return chooseModelList;
}
}
