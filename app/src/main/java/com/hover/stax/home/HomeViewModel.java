package com.hover.stax.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.api.Hover;
import com.hover.sdk.sims.SimInfo;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {

private MutableLiveData<List<BalanceModel>> balances;
private DatabaseRepo repo;


public HomeViewModel(Application application) {
	super(application);
	repo = new DatabaseRepo(application);
	balances = new MutableLiveData<>();
	balances.setValue(new ArrayList<>());
}

public LiveData<List<BalanceModel>> loadBalance() {
	return balances;
}

public void getBalanceFunction() {
	List<HoverAction> balanceActions = repo.getActionsWithBalanceType();
	List<Channel> selectedChannels = repo.getSelected().getValue();

	if(selectedChannels !=null && balanceActions !=null) {
		List<String> simHniList = new ArrayList<>();
		for (SimInfo sim: Hover.getPresentSims(ApplicationInstance.getContext())) {
			if (!simHniList.contains(sim.getOSReportedHni()))
				simHniList.add(sim.getOSReportedHni());
		}

		List<Channel> selectedChannelInSIM = Utils.getSimChannels(selectedChannels, simHniList);

		ArrayList<BalanceModel> balanceModelList = new ArrayList<>();

		for(Channel channel : selectedChannelInSIM) {
			for(HoverAction action: balanceActions) {
				if(action.channelId == channel.id) {
					balanceModelList.add(new BalanceModel(channel.name, action.id, channel.pin));
				}
			}
		}

		balances.postValue(balanceModelList);
	}


}


}