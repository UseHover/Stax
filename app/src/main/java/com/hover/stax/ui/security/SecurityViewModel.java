package com.hover.stax.ui.security;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SecurityViewModel extends ViewModel {
private MutableLiveData<String> mText;

public SecurityViewModel() {
	mText = new MutableLiveData<>();
	mText.setValue("This is Security fragment");
}
public LiveData<String> getText() {
	return mText;
}
}
