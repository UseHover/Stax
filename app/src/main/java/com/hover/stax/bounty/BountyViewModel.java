package com.hover.stax.bounty;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.transactions.StaxTransaction;
import com.hover.stax.utils.Constants;
import com.hover.stax.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BountyViewModel extends AndroidViewModel {
	private static String TAG = "BountyViewModel";

	private DatabaseRepo repo;
	private MutableLiveData<String> emailLiveData;
	private LiveData<List<Action>> actionsForBountyLiveData;
	private MutableLiveData<List<StaxTransaction>> staxTransactionsLiveData;
	private MediatorLiveData<List<BountyAction>> bountyActionsLiveData;
	private MutableLiveData<String> uploadBountyUserResultLiveData;

	public BountyViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		emailLiveData = new MutableLiveData<>();
		uploadBountyUserResultLiveData = new MutableLiveData<>();

		staxTransactionsLiveData = new MutableLiveData<>();
		bountyActionsLiveData = new MediatorLiveData<>();

		actionsForBountyLiveData = Transformations.switchMap(repo.getBountyTransactions(),this::loadBountyActions);
		bountyActionsLiveData.addSource(actionsForBountyLiveData, this::setBountyActions);
	}

	private void setBountyActions(List<Action> filteredActions) {
		List<BountyAction> bountyActions = new ArrayList<>();
		List<StaxTransaction> bountyTransactions = staxTransactionsLiveData.getValue();
		if(bountyTransactions == null) bountyTransactions = new ArrayList<>();

		Map<String, String> actionAndTransactionMap = new HashMap<>();
		for(StaxTransaction transaction : bountyTransactions) {
			//Because list is queried in descending order, lastTransaction will be retained
			actionAndTransactionMap.put(transaction.action_id, transaction.uuid);
		}
		for(Action action : filteredActions) {
			BountyAction bountyAction = new BountyAction();
			bountyAction.a = action;
			bountyAction.lastTransactionUUID = actionAndTransactionMap.get(bountyAction.a.public_id);
			bountyActions.add(bountyAction);
		}
		bountyActionsLiveData.setValue(bountyActions);
	}

	private LiveData<List<Action>> loadBountyActions(List<StaxTransaction> staxTransactions) {
		staxTransactionsLiveData.postValue(staxTransactions);
		return repo.getLiveActionsForBounty();
	}
	public LiveData<List<BountyAction>> getBountyActionsLiveData() {
		if(bountyActionsLiveData == null) bountyActionsLiveData = new MediatorLiveData<>();
		return bountyActionsLiveData;
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
