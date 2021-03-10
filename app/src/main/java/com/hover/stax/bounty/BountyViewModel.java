package com.hover.stax.bounty;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class BountyViewModel extends AndroidViewModel {
	private static String TAG = "BountyViewModel";

	private DatabaseRepo repo;
	private MutableLiveData<String> emailLiveData;
	private Listener listener;
	private MutableLiveData<List<BountyAction>> filteredBountyActions;

	public BountyViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		emailLiveData = new MutableLiveData<>();
		filteredBountyActions = new MutableLiveData<>();
		loadBountyActions();

	}

	private void loadBountyActions() {
		new Thread(() -> filteredBountyActions.postValue(filterBountyActions(repo.getBountyActions()))).start();
	}

	public LiveData<List<BountyAction>> getFilteredBountyActionsLiveData() {
		if (filteredBountyActions == null) filteredBountyActions = new MutableLiveData<>();
		return filteredBountyActions;
	}

	private List<BountyAction> filterBountyActions(List<Action> actions) {
		List<BountyAction> bountyActions = new ArrayList<>();
		for (Action a : actions) {
			BountyAction ba = new BountyAction();
			ba.a = a;
			ba.lastTransactionUUID = null; //TODO: Make bounty actions show pending/done states
			bountyActions.add(ba);
		}
		return bountyActions;
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	public void setEmail(String email) {
		emailLiveData.postValue(email);
	}

	public String emailError() {
		if (Utils.validateEmail(emailLiveData.getValue())) return null;
		else return getApplication().getString(R.string.email_error);
	}

	public void saveBountyUser() {
		String emailValue = emailLiveData.getValue();
		String deviceId = com.hover.sdk.utils.Utils.getDeviceId(getApplication().getApplicationContext());
		assert emailValue != null;
		emailValue = emailValue.replace(" ", ""); //Remove un-necessary spacing causing bug
		repo.insert(new BountyUser(deviceId, emailValue));
	}

	void setBountyUserSize() {
		DatabaseRepo repo = new DatabaseRepo(getApplication());
		new Thread(() -> {
			int count = repo.getBountyUserCount();
			listener.promptEmailOrNavigateBountyList(count);
		}).start();
	}

	public interface Listener {
		void promptEmailOrNavigateBountyList(int bountyUserEntrySize);
	}


}
