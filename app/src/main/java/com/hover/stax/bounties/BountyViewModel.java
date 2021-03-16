package com.hover.stax.bounties;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.sdk.actions.HoverAction;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.transactions.StaxTransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

public class BountyViewModel extends AndroidViewModel {
	private static String TAG = "BountyViewModel";

	private DatabaseRepo repo;

	private LiveData<List<HoverAction>> bountyActions;
	private LiveData<List<Channel>> bountyChannels;
	private LiveData<List<StaxTransaction>> bountyTransactions;
	private MediatorLiveData<List<Bounty>> bountyList = new MediatorLiveData<>();

	public BountyViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);

		bountyActions = repo.getBountyActions();
		bountyChannels = Transformations.switchMap(bountyActions, this::loadChannels);
		bountyTransactions = repo.getBountyTransactions();

		bountyList.addSource(bountyActions, this::makeBounties);
		bountyList.addSource(bountyTransactions, this::makeBountiesIfActions);
	}

	private LiveData<List<Channel>> loadChannels(List<HoverAction> actions) {
		if (actions == null) return new MutableLiveData<>();
		int[] ids = new int[actions.size()];
		for (int a = 0; a < actions.size(); a++)
			ids[a] = actions.get(a).channel_id;
		return repo.getChannels(ids);
	}

	public LiveData<List<HoverAction>> getActions() { return bountyActions; }
	public LiveData<List<Channel>> getChannels() { return bountyChannels; }
	public LiveData<List<StaxTransaction>> getTransactions() { return bountyTransactions; }
	public LiveData<List<Bounty>> getBounties() { return bountyList; }

	private void makeBountiesIfActions(List<StaxTransaction> transactions) {
		if (bountyActions.getValue() != null && transactions != null)
			makeBounties(bountyActions.getValue(), transactions);
	}
	private void makeBounties(List<HoverAction> actions) {
		if (actions != null)
			makeBounties(actions, bountyTransactions.getValue());
	}

	private void makeBounties(List<HoverAction> actions, List<StaxTransaction> transactions) {
		List<Bounty> bounties = new ArrayList<>();
		List<StaxTransaction> transactionsCopy = transactions == null ? new ArrayList<>() : new ArrayList<>(transactions);

		for (HoverAction action : actions) {
			List<StaxTransaction> filterTransactions = new ArrayList<>();

			ListIterator<StaxTransaction> iter = transactionsCopy.listIterator();
			while(iter.hasNext()) {
				StaxTransaction t = iter.next();
				if (t.action_id.equals(action.public_id)) {
					filterTransactions.add(t);
					iter.remove();
				}
			}
			String mostRecentTransactionUUID = filterTransactions.size() > 0 ? filterTransactions.get(filterTransactions.size()-1).uuid : null;
			bounties.add(new Bounty(action, mostRecentTransactionUUID, filterTransactions.size()));
		}
		bountyList.setValue(bounties);
	}
}
