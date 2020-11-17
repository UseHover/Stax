package com.hover.stax.requestAccount;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.hover.stax.R;
import com.hover.stax.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RequestAccountViewModel extends AndroidViewModel {
	private MutableLiveData<RequestAccountStage> requestAccountStageMutableLiveData;
	private MutableLiveData<List<SupportedCountries>> countriesMutableLiveData;

	private MutableLiveData<Integer> phoneError;
	private MutableLiveData<Integer> emailError;


	public RequestAccountViewModel(@NonNull Application application) {
		super(application);

		requestAccountStageMutableLiveData = new MutableLiveData<>();
		countriesMutableLiveData = new MutableLiveData<>();
		phoneError = new MutableLiveData<>();
		emailError = new MutableLiveData<>();


		requestAccountStageMutableLiveData.setValue(RequestAccountStage.SELECT_COUNTRY);
		countriesMutableLiveData.setValue(getSupportedCountries());
		phoneError.setValue(null);
		emailError.setValue(null);

	}

	public MutableLiveData<RequestAccountStage> getRequestAccountStageMutableLiveData() { return requestAccountStageMutableLiveData; }
	public MutableLiveData<List<SupportedCountries>> getCountriesMutableLiveData() { return countriesMutableLiveData; }
	public void setNextRequestAccountStage(RequestAccountStage stage) { requestAccountStageMutableLiveData.postValue(stage.next()); }

	LiveData<Integer> getPhoneError() { return phoneError; }
	LiveData<Integer> getEmailError() { return emailError; }


	public boolean validateContactEntries(String phone, String email) {
		if(phone.isEmpty() && email.isEmpty()) {
			phoneError.postValue(R.string.phone_and_email_input_empty);
			emailError.postValue(R.string.phone_and_email_input_empty);
			return false;
		}
		if(!phone.isEmpty() && phone.length() < 7 ){
			phoneError.postValue(R.string.phone_input_err);
			return false;
		}
		if(!email.isEmpty() && !Utils.validateEmail(email)) {
			emailError.postValue(R.string.email_input_err);
			return false;
		}
		return true;
	}

	public void sendAccountRequestInfoToFirebase(String selectedCountry, String selectedNetwork, String deviceId) {
		Map<String, Object> map = new HashMap<>();
		map.put("deviceId", deviceId);
		map.put("country", selectedCountry);
		map.put("service", selectedNetwork);
		map.put("timeStamp", Timestamp.now());
		FirebaseFirestore.getInstance().collection("requests_from_users").add(map);
	}
	public void sendContactInfoToFirebase(String phone, String email, String deviceId) {
		Map<String, Object> map = new HashMap<>();
		map.put("deviceId", deviceId);
		map.put("phone", phone);
		map.put("email", email);
		map.put("timeStamp", Timestamp.now());
		FirebaseFirestore.getInstance().collection("users_contact").add(map);
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
