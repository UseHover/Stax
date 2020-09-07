package com.hover.stax.home;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.api.Hover;
import com.hover.sdk.sims.SimInfo;
import com.hover.sdk.transactions.Transaction;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.database.KeyStoreExecutor;
import com.hover.stax.utils.Utils;

import org.json.JSONException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
	private LiveData<List<Channel>> selectedChannels = new MutableLiveData<>();
	private LiveData<List<Action>> balanceActions;

	private DatabaseRepo repo;

	public HomeViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		selectedChannels = repo.getSelected();
		balanceActions = Transformations.switchMap(selectedChannels, this::loadBalanceActions);
	}

	public LiveData<List<Action>> loadBalanceActions(List<Channel> channelList) {
		int[] ids = new int[channelList.size()];
		for (int c = 0; c < channelList.size(); c++) {
			ids[c] = channelList.get(c).id;
		}
		return repo.getActions(ids, "balance");
	}

	public LiveData<List<Channel>> getSelectedChannels() { return selectedChannels; }

	public Channel getChannel(int id) {
		List<Channel> allChannels = selectedChannels.getValue() != null ? selectedChannels.getValue() : new ArrayList<>();
		for (Channel channel : allChannels) {
			if (channel.id == id) { return channel; }
		}
		return null;
	}

	public LiveData<List<Action>> getBalanceActions() { return balanceActions; }

	public Action getAction(String public_id) { return repo.getAction(public_id); }
}