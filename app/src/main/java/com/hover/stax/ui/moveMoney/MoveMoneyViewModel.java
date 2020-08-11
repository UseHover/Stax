package com.hover.stax.ui.moveMoney;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MoveMoneyViewModel extends ViewModel {

private MutableLiveData<String> mText;

public MoveMoneyViewModel() {
	mText = new MutableLiveData<>();
	mText.setValue("This is MoveMoney fragment");
}

public LiveData<String> getText() {
	return mText;
}
}