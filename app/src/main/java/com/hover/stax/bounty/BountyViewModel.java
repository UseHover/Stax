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
import com.hover.stax.utils.Utils;
import com.yariksoffice.lingver.Lingver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BountyViewModel extends AndroidViewModel {
	private static String TAG = "BountyViewModel";

	private DatabaseRepo repo;
	private MutableLiveData<String> emailLiveData;
	private LiveData<List<Action>> actionsForBountyLiveData;
	private MutableLiveData<List<StaxTransaction>> staxTransactionsLiveData;
	private MediatorLiveData<List<SectionedBountyAction>> bountyActionsLiveData;
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

	private void setBountyActions(List<Action> filteredActionsForBounty) {

		List<StaxTransaction> bountyTransactions = staxTransactionsLiveData.getValue();
		if(bountyTransactions == null) bountyTransactions = new ArrayList<>();

		Map<String, String> actionAndTransactionMap = getActionAndTransactionMap(bountyTransactions); //Map<action.id, transaction.uuid>
		List<BountyAction> bountyActions = getBountyActionsList(filteredActionsForBounty, actionAndTransactionMap);
		Map<String, List<BountyAction>> sectionedBountyActionsMap = getSectionedBountyActionMap(bountyActions);
		List<SectionedBountyAction> sectionedBountyActionList = getSectionedBountyActionsList(sectionedBountyActionsMap);

		bountyActionsLiveData.setValue(sectionedBountyActionList);
	}

	private Map<String, String> getActionAndTransactionMap(List<StaxTransaction> bountyTransactions) {
		Map<String, String> actionAndTransactionMap = new HashMap<>();
		for(StaxTransaction transaction : bountyTransactions) {
			//Because list is queried in descending order, lastTransaction will be retained
			actionAndTransactionMap.put(transaction.action_id, transaction.uuid);
		}
		return actionAndTransactionMap;
	}
	private List<BountyAction> getBountyActionsList(List<Action> filteredActions, Map<String, String> actionAndTransactionMap) {
		List<BountyAction> resultBountyActions = new ArrayList<>();
		for(Action action : filteredActions) {
			BountyAction bountyAction = new BountyAction();
			bountyAction.a = action;
			bountyAction.lastTransactionUUID = actionAndTransactionMap.get(bountyAction.a.public_id);
			resultBountyActions.add(bountyAction);
		}
		return resultBountyActions;
	}
	private Map<String, List<BountyAction>> getSectionedBountyActionMap(List<BountyAction> bountyActions) {
		Map<String, List<BountyAction>> sectionedBountyActionsMap = new HashMap<>();
		for(BountyAction bountyAction: bountyActions) {
			String rootCode = bountyAction.a.root_code;
			String country = new Locale(Lingver.getInstance().getLanguage(), bountyAction.a.country_alpha2).getDisplayCountry();
			String header = rootCode + " - " + country;
			if(sectionedBountyActionsMap.containsKey(header)) {
				Objects.requireNonNull(sectionedBountyActionsMap.get(header)).add(bountyAction);
			}
			else {
				List<BountyAction> newBountyList = new ArrayList<>();
				newBountyList.add(bountyAction);
				sectionedBountyActionsMap.put(header, newBountyList);
			}
		}
		return  sectionedBountyActionsMap;
	}
	private List<SectionedBountyAction> getSectionedBountyActionsList(Map<String, List<BountyAction>>  sectionedBountyActionsMap) {
		List<SectionedBountyAction> sectionedBountyActionList = new ArrayList<>();
		for (Map.Entry<String, List<BountyAction>> entry : sectionedBountyActionsMap.entrySet()) {
			sectionedBountyActionList.add(new SectionedBountyAction(entry.getKey(), entry.getValue()));
		}
		return sectionedBountyActionList;
	}

	private LiveData<List<Action>> loadBountyActions(List<StaxTransaction> staxTransactions) {
		staxTransactionsLiveData.postValue(staxTransactions);
		return repo.getLiveActionsForBounty();
	}
	public LiveData<List<SectionedBountyAction>> getBountyActionsLiveData() {
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
