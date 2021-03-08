package com.hover.stax.bounty;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.utils.Utils;

public class BountyViewModel extends AndroidViewModel {
	private static String TAG = "BountyViewModel";

	private DatabaseRepo repo;
	private MutableLiveData<String> emailLiveData;
	public BountyViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		emailLiveData = new MutableLiveData<>();
	}

	public void setEmail(String email) {
		emailLiveData.postValue(email);
	}

	public LiveData<Integer> bountyUserCountLiveData() {
		return repo.getBountyUserEntryCount();
	}

	public void saveBountyUser() {
		String emailValue = emailLiveData.getValue();
		String deviceId = Utils.getDeviceId(getApplication().getApplicationContext());
		repo.insert(new BountyUser(emailValue, deviceId));
	}


}
