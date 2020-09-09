package com.hover.stax.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.hover.sdk.api.Hover;
import com.hover.sdk.sims.SimInfo;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.actions.Action;
import com.hover.stax.actions.ActionDao;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelDao;

import java.util.List;

public class DatabaseRepo {
	private ChannelDao channelDao;
	private ActionDao actionDao;

	private LiveData<List<Channel>> allChannels;
	private LiveData<List<Channel>> selectedChannels;
	private Channel defaultChannel;

	public DatabaseRepo(Application application) {
		AppDatabase db = AppDatabase.getInstance(application);
		channelDao = db.channelDao();

		SdkDatabase sdkDb = SdkDatabase.getInstance(application);
		actionDao = sdkDb.actionDao();

		allChannels = channelDao.getAll();
		selectedChannels = channelDao.getSelected(true);
	}

	// Room executes all queries on a separate thread.
// Observed LiveData will notify the observer when the data has changed.
	public Channel getChannel(int id) {
		return channelDao.getChannel(id);
	}

	public LiveData<List<Channel>> getAll() {
		return allChannels;
	}

	public LiveData<List<Channel>> getSelected() { return selectedChannels; }

	public LiveData<Channel> getDefault() { return channelDao.getDefault(); }

	public void insert(Channel channel) {
		AppDatabase.databaseWriteExecutor.execute(() -> channelDao.insert(channel));
	}

	public void update(Channel channel) {
		AppDatabase.databaseWriteExecutor.execute(() -> channelDao.update(channel));
	}

	public List<SimInfo> getSims() {
		return Hover.getPresentSims(ApplicationInstance.getContext());
	}

	public Action getAction(String public_id) {
		return actionDao.getAction(public_id);
	}

	public LiveData<List<Action>> getActions(int channelId, String type) {
		return actionDao.getActions(channelId, type);
	}

	public LiveData<List<Action>> getActions(int[] channelIds, String type) {
		return actionDao.getActions(channelIds, type);
	}

	public LiveData<List<Action>> getActions() {
		return actionDao.getAll();
	}
}
