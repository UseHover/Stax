package com.hover.stax.ui.buyAirtime;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BuyAirtimeViewModel extends ViewModel {

private MutableLiveData<String> mText;

public BuyAirtimeViewModel() {
	mText = new MutableLiveData<>();
	mText.setValue("This is buy airtime fragment");
}

public LiveData<String> getText() {
	return mText;
}
}