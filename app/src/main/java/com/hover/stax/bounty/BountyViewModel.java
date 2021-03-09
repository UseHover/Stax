package com.hover.stax.bounty;

import android.app.Application;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.R;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.utils.Utils;

public class BountyViewModel extends AndroidViewModel {
	private static String TAG = "BountyViewModel";

	private DatabaseRepo repo;
	private MutableLiveData<String> emailLiveData;
	private Listener listener;
	public BountyViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		emailLiveData = new MutableLiveData<>();
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}
	public void setEmail(String email) {
		emailLiveData.postValue(email);
	}
	public String emailError() {
		if(Utils.validateEmail(emailLiveData.getValue())) return null;
		else return getApplication().getString(R.string.email_error);
	}

	public void saveBountyUser() {
		String emailValue = emailLiveData.getValue();
		String deviceId = Utils.getDeviceId(getApplication().getApplicationContext());
		assert emailValue != null;
		repo.insert(new BountyUser(emailValue, deviceId));
	}
	void setBountyUserSize(){
		DatabaseRepo repo = new DatabaseRepo(getApplication());
		new Thread(() -> {
			int count  = repo.getBountyUserCount();
			listener.promptEmailOrNavigateBountyList(count);
		}).start();
	}
	public interface Listener {
		void promptEmailOrNavigateBountyList(int bountyUserEntrySize);
	}


}
