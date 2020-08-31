package com.hover.stax.pins;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.api.Hover;
import com.hover.sdk.sims.SimInfo;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.database.KeyStoreExecutor;
import com.hover.stax.home.BalanceModel;
import com.hover.stax.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class PinsViewModel extends AndroidViewModel {

	private DatabaseRepo repo;

	private LiveData<List<Channel>> channels;
	private MutableLiveData<List<BalanceModel>> balances;

	public PinsViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		loadSelectedChannels();
	}

	public LiveData<List<Channel>> getSelectedChannels() { return channels; }
	public LiveData<List<BalanceModel>> getBalances() {return balances;}

	private void loadSelectedChannels() {
		if (channels == null) {
			channels = new MutableLiveData<>();
		}
		channels = repo.getSelected();

		balances = new MutableLiveData<>();
		balances.setValue(new ArrayList<>());
	}

	void savePins(List<Channel> updatedChannels, Context c) {

		for (Channel channel : updatedChannels) {
			if (channel.pin != null) {
				channel.pin = KeyStoreExecutor.createNewKey(channel.pin, c);

				repo.update(channel);
			}
		}

		List<HoverAction> balanceActions = repo.getActionsWithBalanceType();
		ArrayList<BalanceModel> balanceModelList = new ArrayList<>();

		if ( balanceActions != null) {
			List<String> simHniList = new ArrayList<>();
			for (SimInfo sim : Hover.getPresentSims(ApplicationInstance.getContext())) {
				if (!simHniList.contains(sim.getOSReportedHni()))
					simHniList.add(sim.getOSReportedHni());
			}

			List<Channel> selectedChannelInSIM = Utils.getSimChannels(updatedChannels, simHniList);


			for (Channel channel : selectedChannelInSIM) {
				for (HoverAction action : balanceActions) {
					if (action.channelId == channel.id) {
						balanceModelList.add(new BalanceModel(channel.name, action.id, channel.pin));
					}
				}
			}
		}

		balances.postValue(balanceModelList);
	}

	public void setDefaultAccount(Channel channel) {
		repo.update(channel);
	}
}
