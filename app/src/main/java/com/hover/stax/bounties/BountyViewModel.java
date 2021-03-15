package com.hover.stax.bounties;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.hover.sdk.actions.HoverAction;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.transactions.StaxTransaction;

import java.util.ArrayList;
import java.util.List;

public class BountyViewModel extends AndroidViewModel {
	private static String TAG = "BountyViewModel";

	private DatabaseRepo repo;

	private LiveData<List<HoverAction>> bountyActions;
	private LiveData<List<StaxTransaction>> bountyTransactions;
	private MediatorLiveData<List<Bounty>> bountyList = new MediatorLiveData<>();

	public BountyViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);

		bountyActions = repo.getBountyActions();
		bountyTransactions = repo.getBountyTransactions();

		bountyList.addSource(bountyActions, this::makeMap);
		bountyList.addSource(bountyTransactions, this::makeMapIfActions);
	}

	public LiveData<List<HoverAction>> getActions() { return bountyActions; }
	public LiveData<List<StaxTransaction>> getTransactions() { return bountyTransactions; }
	public LiveData<List<Bounty>> getMap() { return bountyList; }

	private void makeMapIfActions(List<StaxTransaction> transactions) {
		if (bountyActions.getValue() != null && transactions != null)
			makeMap(bountyActions.getValue(), transactions);
	}
	private void makeMap(List<HoverAction> actions) {
		if (actions != null)
			makeMap(actions, bountyTransactions.getValue());
	}

	private void makeMap(List<HoverAction> actions, List<StaxTransaction> transactions) {
		List<Bounty> bounties = new ArrayList<>();
		for (HoverAction action : actions) {
			List<StaxTransaction> transactionsCopy = transactions == null ? new ArrayList<>() : transactions;
			List<StaxTransaction> filterTransactions = new ArrayList<>();

			for (StaxTransaction transaction : transactionsCopy) {
				if (transaction.action_id.equals(action.public_id)) {
					filterTransactions.add(transaction);
					transactions.remove(transaction);
				}
			}
			bounties.add(new Bounty(action, filterTransactions));
		}
		bountyList.setValue(bounties);
	}
}
