package com.hover.stax.channels;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.amplitude.api.Amplitude;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hover.sdk.api.Hover;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.requests.Request;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.sims.Sim;
import com.hover.stax.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChannelDropdownViewModel extends AndroidViewModel implements ChannelDropdown.HighlightListener {
	public final static String TAG = "ChannelDropdownVM";

	private DatabaseRepo repo;
	private MutableLiveData<String> type = new MutableLiveData<>();

	private MutableLiveData<List<Sim>> sims;
	private LiveData<List<String>> simHniList = new MutableLiveData<>();

	private LiveData<List<Channel>> allChannels;
	private LiveData<List<Channel>> selectedChannels;
	private MediatorLiveData<List<Channel>> simChannels;
	private MediatorLiveData<Channel> activeChannel = new MediatorLiveData<>();
	private MediatorLiveData<List<Action>> channelActions = new MediatorLiveData<>();

	private MediatorLiveData<String> error = new MediatorLiveData<>();
	private MediatorLiveData<Integer> helper = new MediatorLiveData<>();

	public ChannelDropdownViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		type.setValue(Action.BALANCE);

		loadChannels();
		loadSims();

		simHniList = Transformations.map(sims, this::getHnisAndSubscribeToEachOnFirebase);

		simChannels = new MediatorLiveData<>();
		simChannels.addSource(allChannels, this::onChannelsUpdateHnis);
		simChannels.addSource(simHniList, this::onSimUpdate);

		activeChannel.addSource(selectedChannels, this::setActiveChannelIfNull);
		error.addSource(activeChannel, channel -> {
			if (channel != null && channelActions.getValue() != null && channelActions.getValue().size() > 0)
				error.setValue(null);
		});

		channelActions.addSource(type, this::loadActions);
		channelActions.addSource(selectedChannels, this::loadActions);
		channelActions.addSource(activeChannel, this::loadActions);
		error.addSource(channelActions, actions -> {
			if (activeChannel.getValue() != null && (actions == null || actions.size() == 0))
				error.setValue(application.getString(R.string.no_actions_fielderror, Action.getHumanFriendlyType(getApplication(), type.getValue())));
			else error.setValue(null);
		});
		helper.addSource(channelActions, actions -> {
			if (actions != null && actions.size() == 1 && !actions.get(0).requiresRecipient() && !actions.get(0).transaction_type.equals(Action.BALANCE))
				helper.setValue(actions.get(0).transaction_type.equals(Action.AIRTIME) ? R.string.self_only_airtime_warning : R.string.self_only_money_warning);
			else helper.setValue(null);
		});
	}

	public void setType(String t) { type.setValue(t); }

	private void loadChannels() {
		if (allChannels == null) { allChannels = new MutableLiveData<>(); }
		if (selectedChannels == null) { selectedChannels = new MutableLiveData<>(); }
		allChannels = repo.getAllChannels();
		selectedChannels = repo.getSelected();
	}

	public LiveData<List<Channel>> getChannels() {
		if (allChannels == null) {
			allChannels = new MutableLiveData<>();
		}
		return allChannels;
	}

	public LiveData<List<Channel>> getSelectedChannels() {
		if (selectedChannels == null) { selectedChannels = new MutableLiveData<>(); }
		return selectedChannels;
	}

	void loadSims() {
		if (sims == null) { sims = new MutableLiveData<>(); }
		new Thread(() -> sims.postValue(repo.getSims())).start();
		LocalBroadcastManager.getInstance(getApplication())
				.registerReceiver(simReceiver, new IntentFilter(Utils.getPackage(getApplication()) + ".NEW_SIM_INFO_ACTION"));
		Hover.updateSimInfo(getApplication());
	}

	private final BroadcastReceiver simReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			new Thread(() -> sims.postValue(repo.getSims())).start();
		}
	};

	public LiveData<List<Sim>> getSims() {
		if (sims == null) { sims = new MutableLiveData<>(); }
		return sims;
	}

	public LiveData<List<String>> getSimHniList() {
		if (simHniList == null) { simHniList = new MutableLiveData<>(); }
		return simHniList;
	}

	private List<String> getHnisAndSubscribeToEachOnFirebase(List<Sim> sims) {
		if (sims == null) return null;
		List<String> hniList = new ArrayList<>();
		for (Sim sim : sims) {
			if (!hniList.contains(sim.hni)) {
				FirebaseMessaging.getInstance().subscribeToTopic("sim-" + sim.hni);
				FirebaseMessaging.getInstance().subscribeToTopic(sim.country_iso);
				hniList.add(sim.hni);
			}
		}
		return hniList;
	}

	private void onChannelsUpdateHnis(List<Channel> channels) {
		updateSimChannels(simChannels, channels, simHniList.getValue());
	}

	private void onSimUpdate(List<String> hniList) {
		updateSimChannels(simChannels, allChannels.getValue(), hniList);
	}

	public void updateSimChannels(MediatorLiveData<List<Channel>> simChannels, List<Channel> channels, List<String> hniList) {
		if (channels == null || hniList == null) return;
		List<Channel> simChannelList = new ArrayList<>();
		for (int i = 0; i < channels.size(); i++) {
			String[] hniArr = channels.get(i).hniList.split(",");
			for (String s : hniArr) {
				if (hniList.contains(Utils.stripHniString(s))) {
					if (!simChannelList.contains(channels.get(i)))
						simChannelList.add(channels.get(i));
				}
			}
		}
		simChannels.setValue(simChannelList);
	}

	public LiveData<List<Channel>> getSimChannels() {
		return simChannels;
	}

	protected void setActiveChannelIfNull(List<Channel> channels) {
		if (channels != null && channels.size() > 0 && activeChannel.getValue() == null) {
			for (Channel c: channels)
				if (c.defaultAccount) { activeChannel.postValue(c); }
		}
	}

	private void setActiveChannel(Channel channel) {
		activeChannel.setValue(channel);
	}

	void setActiveChannel(List<Action> acts) {
		if (acts == null || acts.size() == 0) { return; }
		activeChannel.removeSource(channelActions);
		new Thread(() -> activeChannel.postValue(repo.getChannel(acts.get(0).channel_id))).start();
	}
	public LiveData<Channel> getActiveChannel() { return activeChannel; }

	@Override
	public void highlightChannel(Channel c) { setActiveChannel(c); }

	public void loadActions(String t) {
		if ((t.equals(Action.BALANCE) && selectedChannels.getValue() == null) || (!t.equals(Action.BALANCE) && activeChannel.getValue() == null)) return;
		if (t.equals(Action.BALANCE))
			loadActions(selectedChannels.getValue(), t);
		else
			loadActions(activeChannel.getValue(), t);
	}

	public void loadActions(Channel channel) {
		loadActions(channel, type.getValue());
	}

	private void loadActions(Channel c, String t) {
		new Thread(() -> channelActions.postValue(t.equals(Action.P2P) ? repo.getTransferActions(c.id) : repo.getActions(c.id, t))).start();
	}

	public void loadActions(List<Channel> channels) {
		if (type.getValue().equals(Action.BALANCE))
			loadActions(channels, type.getValue());
	}

	public void loadActions(List<Channel> channels, String t) {
		int[] ids = new int[channels.size()];
		for (int c = 0; c < channels.size(); c++)
			ids[c] = channels.get(c).id;
		new Thread(() -> channelActions.postValue(repo.getActions(ids, t))).start();
	}

	public LiveData<List<Action>> getChannelActions() { return channelActions; }

	public void setChannelSelected(Channel channel) {
		if (channel == null || channel.selected) return;
		logChoice(channel);
		channel.selected = true;
		channel.defaultAccount = selectedChannels.getValue() == null || selectedChannels.getValue().size() == 0;
		repo.update(channel);
	}

	private void logChoice(Channel channel) {
		Log.i(TAG, "saving selected channel: " + channel);
		FirebaseMessaging.getInstance().subscribeToTopic("channel-" + channel.id);
		JSONObject event = new JSONObject();
		try { event.put(getApplication().getString(R.string.added_channel_id), channel.id);
		} catch (JSONException ignored) { }
		Amplitude.getInstance().logEvent(getApplication().getString(R.string.new_channel_selected), event);
	}

	public boolean validates() {
		boolean valid = true;
		if (activeChannel.getValue() == null) {
			valid = false;
			error.setValue(getApplication().getString(R.string.channels_error_noselect));
		} else if (channelActions.getValue() == null || channelActions.getValue().size() == 0) {
			valid = false;
			error.setValue(getApplication().getString(R.string.no_actions_fielderror, Action.getHumanFriendlyType(getApplication(), type.getValue())));
		}
		return valid;
	}

	public LiveData<String> getError() {
		if (error == null) { error = new MediatorLiveData<>(); }
		return error;
	}

	public LiveData<Integer> getHelper() {
		if (helper == null) { helper = new MediatorLiveData<>(); }
		return helper;
	}

	public void setChannelFromRequest(Request r) {
		if (r != null && selectedChannels.getValue() != null && selectedChannels.getValue().size() > 0) {
			new Thread(() -> {
				List<Action> acts = repo.getActions(getChannelIds(selectedChannels.getValue()), r.requester_institution_id);
				if (acts.size() <= 0) {
					acts = repo.getActions(getChannelIds(simChannels.getValue()), r.requester_institution_id);
					if (acts.size() <= 0)
						error.postValue(getApplication().getString(R.string.channel_request_fielderror, String.valueOf(r.requester_institution_id)));
				}
				channelActions.postValue(acts);
			}).start();
			activeChannel.addSource(channelActions, this::setActiveChannel);
		}
	}

	private int[] getChannelIds(List<Channel> channels) {
		int[] ids = new int[channels.size()];
		for (int c = 0; c < channels.size(); c++)
			ids[c] = channels.get(c).id;
		return ids;
	}

	public void view(Schedule s) {
		setType(s.type);
		setActiveChannel(repo.getChannel(s.channel_id));
	}

	@Override
	protected void onCleared() {
		try {
			LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(simReceiver);
		} catch (Exception ignored) {
		}
		super.onCleared();
	}
}
