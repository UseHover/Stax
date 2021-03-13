package com.hover.stax.bounties;

import android.app.Application;
import android.view.SurfaceControl;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.transactions.StaxTransaction;
import com.hover.stax.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class BountyViewModel extends AndroidViewModel {
	private static String TAG = "BountyViewModel";

	private DatabaseRepo repo;
	private MutableLiveData<String> emailLiveData;
	private MutableLiveData<String> uploadBountyUserResultLiveData;

	private LiveData<List<Action>> bountyActions;
	private LiveData<List<StaxTransaction>> bountyTransactions;
	private MediatorLiveData<Map<Action, List<StaxTransaction>>> bountyMap = new MediatorLiveData<>();

	public BountyViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		emailLiveData = new MutableLiveData<>();
		uploadBountyUserResultLiveData = new MutableLiveData<>();

		bountyActions = repo.getBountyActions();
		bountyTransactions = repo.getBountyTransactions();

		bountyMap.addSource(bountyActions, this::makeMap);
		bountyMap.addSource(bountyTransactions, this::makeMapIfActions);
	}

	public LiveData<List<Action>> getActions() { return bountyActions; }
	public LiveData<List<StaxTransaction>> getTransactions() { return bountyTransactions; }
	public LiveData<Map<Action, List<StaxTransaction>>> getMap() { return bountyMap; }

	private void makeMapIfActions(List<StaxTransaction> transactions) {
		if (bountyActions.getValue() != null && transactions != null)
			makeMap(bountyActions.getValue(), transactions);
	}
	private void makeMap(List<Action> actions) {
		if (actions != null)
			makeMap(actions, bountyTransactions.getValue());
	}

	private void makeMap(List<Action> actions, List<StaxTransaction> transactions) {
		Map<Action, List<StaxTransaction>> actionTransactionMap = new HashMap<>();
		for (Action action : actions) {
			List<StaxTransaction> transactionsCopy = transactions == null ? new ArrayList<>() : transactions;
			List<StaxTransaction> filterTransactions = new ArrayList<>();

			for (StaxTransaction transaction : transactionsCopy) {
				if (transaction.action_id.equals(action.public_id)) {
					filterTransactions.add(transaction);
					transactions.remove(transaction);
				}
			}
			actionTransactionMap.put(action, filterTransactions);
		}
		bountyMap.setValue(actionTransactionMap);
	}

	public void setEmail(String email) {
		emailLiveData.postValue(email);
	}
	public String getEmail() {
		return emailLiveData.getValue();
	}

	public String emailError() {
		if (Utils.validateEmail(emailLiveData.getValue())) return null;
		else return getApplication().getString(R.string.email_error);
	}

	public void setUploadBountyUserResultLiveData(String result) {
		uploadBountyUserResultLiveData.postValue(result);
	}
	public LiveData<String> getUploadBountyResult() {
		return uploadBountyUserResultLiveData;
	}
}
