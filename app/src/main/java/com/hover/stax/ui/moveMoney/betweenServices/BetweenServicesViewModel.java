package com.hover.stax.ui.moveMoney.betweenServices;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hover.stax.database.ConvertRawDatabaseDataToModels;
import com.hover.stax.models.BetweenServicesDataModel;
import com.hover.stax.models.StaxGetServiceAndActionModel;

import java.util.ArrayList;

public class BetweenServicesViewModel extends ViewModel {
private MutableLiveData<ArrayList<BetweenServicesDataModel>> serviceAndActionModels;

public BetweenServicesViewModel() {
	serviceAndActionModels = new MutableLiveData<>();
}

public void getBetweenServicesData() {
	serviceAndActionModels.postValue(new ConvertRawDatabaseDataToModels().getBetweenServicesData());
}

public LiveData<ArrayList<BetweenServicesDataModel>> loadBetweenServicesData() {
	return serviceAndActionModels;
}
}
