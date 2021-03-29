package com.hover.stax.bounties;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.api.Hover;
import com.hover.sdk.sims.SimInfo;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.transactions.StaxTransaction;
import com.hover.stax.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class BountyViewModel extends AndroidViewModel {
	private static String TAG = "BountyViewModel";

	private DatabaseRepo repo;

	private LiveData<List<HoverAction>> bountyActions;
	private LiveData<List<Channel>> bountyChannels;
	private MutableLiveData<List<Channel>> filteredBountyChannels;
	private LiveData<List<StaxTransaction>> bountyTransactions;
	private MutableLiveData<Bounty> simPresentBounty;
	private MutableLiveData<List<SimInfo>> sims;
	private LiveData<Double[]> simHniList = new MutableLiveData<>();
	private MediatorLiveData<List<Bounty>> bountyList = new MediatorLiveData<>();

	public BountyViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);

		loadSims();
		simHniList = Transformations.map(sims, this::setHnis);

		filteredBountyChannels = new MutableLiveData<>();
		filteredBountyChannels.setValue(null);


		bountyActions = repo.getBountyActions();
		bountyChannels = Transformations.switchMap(bountyActions, this::loadChannels);
		bountyTransactions = repo.getBountyTransactions();

		bountyList.addSource(bountyActions, this::makeBounties);
		bountyList.addSource(bountyTransactions, this::makeBountiesIfActions);

	}

	public LiveData<Double[]> getSimHniList() {
		return simHniList;
	}

	public MutableLiveData<List<SimInfo>> getSims() {
		return sims;
	}

	private LiveData<List<Channel>> loadChannels(List<HoverAction> actions) {
		if (actions == null) return new MutableLiveData<>();
		int[] ids = getChannelIdArray(actions);
		return repo.getChannels(ids);
	}


	public LiveData<List<HoverAction>> getActions() { return bountyActions; }
	public LiveData<List<Channel>> getChannels() { return bountyChannels; }
	public LiveData<List<StaxTransaction>> getTransactions() { return bountyTransactions; }
	public LiveData<List<Bounty>> getBounties() { return bountyList; }
	public LiveData<Bounty> getSimSupportedBounty() {
		 if(simPresentBounty == null) simPresentBounty = new MutableLiveData<>();
		 return simPresentBounty;
	}

	public LiveData<List<Channel>> filterChannels(String countryCode){
		List<HoverAction> actions = bountyActions.getValue();
		if(actions == null) return null;
		return repo.getChannelsByCountry(getChannelIdArray(actions), countryCode);
	}

	private int[] getChannelIdArray(List<HoverAction> actions) {
		int[] ids = new int[actions.size()];
		for (int a = 0; a < actions.size(); a++)
			ids[a] = actions.get(a).channel_id;

		return ids;
	}

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
			bounties.add(new Bounty(action, filterTransactions));
		}
		bountyList.setValue(bounties);
	}

	private Double[] setHnis(List<SimInfo> sims) {
		if (sims == null) return null;
		Set<Double> hniList = new HashSet<>();
		for (SimInfo sim : sims) {
				hniList.add(Double.parseDouble(sim.getOSReportedHni()));
		}
		Double[] result =  hniList.toArray(new Double[0]);
		Arrays.sort(result);
		return result;
	}
	void loadSims() {
		if (sims == null) { sims = new MutableLiveData<>(); }
		new Thread(() -> sims.postValue(repo.getSims())).start();
		LocalBroadcastManager.getInstance(getApplication())
				.registerReceiver(simReceiver, new IntentFilter(com.hover.stax.utils.Utils.getPackage(getApplication()) + ".NEW_SIM_INFO_ACTION"));
		Hover.updateSimInfo(getApplication());
	}

	private final BroadcastReceiver simReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			new Thread(() -> sims.postValue(repo.getSims())).start();
		}
	};

	public void setSimPresentBounty(Bounty b) {
		new Thread(() -> {
			double[] hnis = Utils.convertJsonArrToDoubleArr(b.action.hni_list);
			Double[] simHnis = simHniList.getValue();
			b.presentSimsSupported = getArraysJointSize(simHnis, hnis);
			simPresentBounty.postValue(b);
		}).start();
	}

	private int getArraysJointSize(Double[] array1, double[] array2) {
		if(array1 ==null || array2 == null) return 0;
		int jointSize = 0;
		for(double value: array1) {
			if(Arrays.binarySearch(array2, value) !=-1) jointSize = jointSize+1;
		}
		return  jointSize;
	}
}
