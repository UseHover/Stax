package com.hover.stax.requestAccount;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


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

	public void sendAccountRequestInfoToAmplitude(String selectedCountry, String selectedNetwork, Context c) {
		JSONObject data = new JSONObject();
		try {
			data.put("deviceId", Utils.getDeviceId(c));
			data.put("country", selectedCountry);
			data.put("service", selectedNetwork);
			data.put("timeStamp", DateUtils.now());
		} catch (JSONException e) {}
		Amplitude.getInstance().logEvent(c.getString(R.string.request_add_service), data);
	}
	public void sendContactInfoToAmplitude(String phone, String email, Context c) {
		JSONObject data = new JSONObject();
		try {
			data.put("deviceId", Utils.getDeviceId(c));
			data.put("phone", phone);
			data.put("email", email);
			data.put("timeStamp", DateUtils.now());
		} catch (JSONException e) {}
		Amplitude.getInstance().logEvent(c.getString(R.string.request_add_service_contact_info), data);
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
