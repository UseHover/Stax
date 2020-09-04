package com.hover.stax.pins;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.api.Hover;
import com.hover.sdk.sims.SimInfo;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.actions.Action;
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
	public LiveData<List<BalanceModel>> getBalances() { return balances; }

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

		ArrayList<BalanceModel> balanceModelList = new ArrayList<>();
		for (Channel channel : updatedChannels) {
			Action action = repo.getActions(channel.id, "balance").getValue().get(0);
			if (Hover.isActionSimPresent(action.public_id, getApplication())) {
				channel.pin = KeyStoreExecutor.decrypt(channel.pin, ApplicationInstance.getContext());
				balanceModelList.add(new BalanceModel(action.public_id, channel, "null", 0));
			}
		}

		balances.setValue(balanceModelList);
	}

	public void clearAllPins(List<Channel> channels) {
		for (Channel channel : channels) {
			channel.pin = null;
			repo.update(channel);
		}
	}

	public void setDefaultAccount(Channel channel) {
		repo.update(channel);
	}
}
