package com.hover.stax.bounties;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

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
import com.hover.stax.countries.CountryAdapter;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.transactions.StaxTransaction;
import com.hover.stax.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class BountyViewModel extends AndroidViewModel {
    private static String TAG = "BountyViewModel";

    private DatabaseRepo repo;

    private LiveData<List<HoverAction>> bountyActions;
    private LiveData<List<Channel>> bountyChannels;
    private LiveData<List<StaxTransaction>> bountyTransactions;

    private MutableLiveData<List<Channel>> filteredBountyChannels;
    private MediatorLiveData<List<Bounty>> bountyList = new MediatorLiveData<>();

    private MutableLiveData<List<SimInfo>> sims;

    public BountyViewModel(@NonNull Application application) {
        super(application);
        repo = new DatabaseRepo(application);
        loadSims();
        filteredBountyChannels = new MutableLiveData<>();
        filteredBountyChannels.setValue(null);

        bountyActions = repo.getBountyActions();
        bountyChannels = Transformations.switchMap(bountyActions, this::loadChannels);
        bountyTransactions = repo.getBountyTransactions();

        bountyList.addSource(bountyActions, this::makeBounties);
        bountyList.addSource(bountyTransactions, this::makeBountiesIfActions);

    }

    void loadSims() {
        if (sims == null) {
            sims = new MutableLiveData<>();
        }
        new Thread(() -> sims.postValue(repo.getPresentSims())).start();
        LocalBroadcastManager.getInstance(getApplication())
                .registerReceiver(simReceiver, new IntentFilter(Utils.getPackage(getApplication()) + ".NEW_SIM_INFO_ACTION"));
        Hover.updateSimInfo(getApplication());
    }

    private final BroadcastReceiver simReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new Thread(() -> sims.postValue(repo.getPresentSims())).start();
        }
    };

    boolean isSimPresent(Bounty b) {
        if (sims.getValue() == null || sims.getValue().size() == 0) return false;
        for (SimInfo sim : sims.getValue()) {
            for (int i = 0; i < b.action.hni_list.length(); i++)
                if (b.action.hni_list.optString(i).equals(sim.getOSReportedHni()))
                    return true;
        }
        return false;
    }

    public LiveData<List<SimInfo>> getSims() {
        if (sims == null) {
            sims = new MutableLiveData<>();
        }
        return sims;
    }

    private LiveData<List<Channel>> loadChannels(List<HoverAction> actions) {
        if (actions == null) return new MutableLiveData<>();
        int[] ids = getChannelIdArray(actions);
        return repo.getChannels(ids);
    }

    public LiveData<List<HoverAction>> getActions() {
        return bountyActions;
    }

    public LiveData<List<Channel>> getChannels() {
        return bountyChannels;
    }

    public LiveData<List<StaxTransaction>> getTransactions() {
        return bountyTransactions;
    }

    public LiveData<List<Bounty>> getBounties() {
        return bountyList;
    }

    public LiveData<List<Channel>> filterChannels(String countryCode) {
        List<HoverAction> actions = bountyActions.getValue();
        if (actions == null) return null;

        if (countryCode.equals(CountryAdapter.codeRepresentingAllCountries()))
            return loadChannels(actions);
        else return repo.getChannelsByCountry(getChannelIdArray(actions), countryCode);
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
            while (iter.hasNext()) {
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
}
