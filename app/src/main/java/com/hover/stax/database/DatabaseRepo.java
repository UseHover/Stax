package com.hover.stax.database;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.api.Hover;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelDao;

import java.util.List;

public class DatabaseRepo {
	private ChannelDao channelDao;
	private LiveData<List<Channel>> allChannels;
	private LiveData<List<Channel>> selectedChannels;

	public DatabaseRepo(Application application) {
		AppDatabase db = AppDatabase.getInstance(application);
		channelDao = db.channelDao();
		allChannels = channelDao.getAll();
		selectedChannels = channelDao.getSelected(true);
	}

	// Room executes all queries on a separate thread.
	// Observed LiveData will notify the observer when the data has changed.
	public LiveData<List<Channel>> getAll() {
		return allChannels;
	}

	public LiveData<List<Channel>> getSelected() {
		return selectedChannels;
	}


	public void insert(Channel channel) {
		AppDatabase.databaseWriteExecutor.execute(() -> channelDao.insert(channel));
	}

	public void update(Channel channel) {
		AppDatabase.databaseWriteExecutor.execute(() -> channelDao.update(channel));
	}

	public List<HoverAction> getActionsWithBalanceType() {
		String balance = "balance";
		String filter = "transaction_type = '" + balance + "'";
		return Hover.getActions(filter, ApplicationInstance.getContext());
	}

}
