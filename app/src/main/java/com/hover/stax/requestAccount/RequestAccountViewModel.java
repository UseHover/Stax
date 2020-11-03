package com.hover.stax.requestAccount;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.sdk.utils.Utils;
import com.hover.stax.R;

import java.util.ArrayList;
import java.util.List;


public class RequestAccountViewModel extends AndroidViewModel {
	private MutableLiveData<RequestAccountStage> requestAccountStageMutableLiveData;
	private MutableLiveData<List<SupportedCountries>> countriesMutableLiveData;

	private MutableLiveData<Integer> phoneError;
	private MutableLiveData<Integer> emailError;
	private MutableLiveData<String> phoneVal;
	private MutableLiveData<String> emailVal;
	public RequestAccountViewModel(@NonNull Application application) {
		super(application);

		requestAccountStageMutableLiveData = new MutableLiveData<>();
		countriesMutableLiveData = new MutableLiveData<>();
		phoneError = new MutableLiveData<>();
		emailError = new MutableLiveData<>();
		phoneVal = new MutableLiveData<>();
		emailVal = new MutableLiveData<>();


		requestAccountStageMutableLiveData.setValue(RequestAccountStage.SELECT_COUNTRY);
		countriesMutableLiveData.setValue(getSupportedCountries());
		phoneError.setValue(null);
		emailError.setValue(null);
		phoneVal.setValue(null);
		emailVal.setValue(null);
	}

	public MutableLiveData<RequestAccountStage> getRequestAccountStageMutableLiveData() { return requestAccountStageMutableLiveData; }
	public MutableLiveData<List<SupportedCountries>> getCountriesMutableLiveData() { return countriesMutableLiveData; }
	public void setNextRequestAccountStage(RequestAccountStage stage) { requestAccountStageMutableLiveData.postValue(stage.next()); }

	LiveData<Integer> getPhoneError() { return phoneError; }
	LiveData<Integer> getEmailError() { return emailError; }
	public MutableLiveData<String> getPhoneVal() { return phoneVal; }

	public void setPhoneVal(String phone) { phoneVal.postValue(phone); }
	public void setEmailVal(String email) { emailVal.postValue(email); }
	public boolean validateContactEntries() {
		if(phoneVal.getValue().length() > 7) return true;
		else (ea)
	}

	public void sendAccountRequestInfoToFirebase(String selectedCountry, String selectedNetwork, String deviceId) {

	}
	public void sendContactInfoToFirebase(String phone, String email, String deviceId) {

	}

	private List<SupportedCountries> getSupportedCountries() {
		String[] countries = getApplication().getResources().getStringArray(R.array.supported_countries);
		ArrayList<SupportedCountries> countryList = new ArrayList<>();
		for (String name : countries) {
			SupportedCountries toAdd = new SupportedCountries(name);
			if (!countryList.contains(toAdd)) countryList.add(toAdd);
		}
		return countryList;
	}

}
