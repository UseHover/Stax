package com.hover.stax.balances;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.amplitude.api.Amplitude;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.utils.UIHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BalancesViewModel extends AndroidViewModel {
	private final String TAG = "BalancesViewModel";

	private final int ALL = -1, NONE = 0;

	private DatabaseRepo repo;
	private RunBalanceListener listener;
	private List<Integer> hasRun = new ArrayList<>();

	private LiveData<List<Channel>> selectedChannels;
	private LiveData<List<Action>> balanceActions = new MediatorLiveData<>();
	private MutableLiveData<Integer> runFlag = new MutableLiveData<>();
	private MediatorLiveData<List<Action>> toRun;
	private MutableLiveData<Channel> highlightedChannel = new MutableLiveData<>();
	private MutableLiveData<Boolean> runBalanceError = new MutableLiveData<>();

	public BalancesViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		if (runFlag == null) runFlag.setValue(NONE);

		selectedChannels = repo.getSelected();
		balanceActions = Transformations.switchMap(selectedChannels, this::loadBalanceActions);

		toRun = new MediatorLiveData<>();
		toRun.setValue(new ArrayList<>());
		toRun.addSource(runFlag, this::onSetRunning);
		toRun.addSource(balanceActions, this::onActionsLoaded);
		runBalanceError.setValue(false);
	}

	public void setListener(RunBalanceListener l) {
		listener = l;
	}

	public LiveData<List<Action>> getToRun() {
		return toRun;
	}

	public LiveData<Integer> getRunFlag() {
		return runFlag;
	}

	public LiveData<List<Channel>> getSelectedChannels() {
		if (selectedChannels == null) {
			selectedChannels = new MutableLiveData<>();
		}
		return selectedChannels;
	}

	public LiveData<List<Action>> loadBalanceActions(List<Channel> channelList) {
		Log.e(TAG, "attempting to load " + channelList.size() + " balance actions");
		int[] ids = new int[channelList.size()];
		for (int c = 0; c < channelList.size(); c++)
			ids[c] = channelList.get(c).id;
		Log.e(TAG, "attempting to load balance actions for channels: " + Arrays.toString(ids));
		return repo.getLiveActions(ids, Action.BALANCE);
	}

	public LiveData<List<Action>> getBalanceActions() {
		return balanceActions;
	}

	public void highlightChannel(Channel channel) {
		highlightedChannel.postValue(channel);
	}

	public Channel getHighlightedChannel() {
		return highlightedChannel.getValue();
	}

	public void selectChannel(Context c) {
		if (highlightedChannel.getValue() == null) return;
		Log.e(TAG, "saving channel as selected");
		Channel channel = highlightedChannel.getValue();
		logChoice(channel, c);
		channel.selected = true;
		channel.defaultAccount = true;
		repo.update(channel);
	}

	private void logChoice(Channel channel, Context c) {
		FirebaseMessaging.getInstance().subscribeToTopic("channel-" + channel.id);
		JSONObject event = new JSONObject();
		try { event.put(c.getString(R.string.added_channel_id), channel.id);
		} catch (JSONException ignored) { }
		Amplitude.getInstance().logEvent(c.getString(R.string.new_account_check_balance), event);
	}


	public Channel getChannel(int id) {
		List<Channel> allChannels = selectedChannels.getValue() != null ? selectedChannels.getValue() : new ArrayList<>();
		return getChannel(allChannels, id);
	}

	public Channel getChannel(List<Channel> channels, int id) {
		for (Channel channel : channels) {
			if (channel.id == id) {
				return channel;
			}
		}
		return null;
	}

	public void setRunning(int channel_id) {
		runFlag.setValue(channel_id);
	}

	public void setAllRunning(Context c) {
		Log.e(TAG, "triggering refresh");
		Amplitude.getInstance().logEvent(c.getString(R.string.refresh_balance_all));
		runFlag.setValue(ALL);
	}

	public void setRunBalanceError(boolean showError) {runBalanceError.postValue(showError);}
	public LiveData<Boolean> getBalanceError() { return runBalanceError; }

	private void onSetRunning(Integer flag) {
		if (flag == null || flag == NONE) toRun.setValue(new ArrayList<>());
		else if (flag == ALL) startRun(balanceActions.getValue());
		else startRun(getChannelActions(flag));
	}

	private void onActionsLoaded(List<Action> actions) {
		if (runFlag.getValue() == null || toRun.getValue().size() > 0) return;
		if (runFlag.getValue() == ALL) startRun(actions);
		else if (runFlag.getValue() != NONE) startRun(getChannelActions(runFlag.getValue()));
	}

	void startRun(List<Action> actions) {
		Log.e(TAG, "refreshing " + actions);
		if (actions == null || actions.size() == 0) return;
		Log.e(TAG, "action count: " + actions.size());
		toRun.setValue(actions);
		runNext(actions, 0);
	}

	private void runNext(List<Action> actions, int index) {
		if (listener != null)
			listener.startRun(actions.get(index), index);
		else
			UIHelper.flashMessage(getApplication(), "Failed to start run, please try again.");
	}

	public void setRan(int index) {
		if (toRun.getValue().size() > index + 1) {
			hasRun.add(toRun.getValue().get(index).id);
			while (hasRun.contains(toRun.getValue().get(index + 1).id))
				index += 1;
			if (toRun.getValue().size() > index + 1)
				runNext(toRun.getValue(), index + 1);
			else endRun();
		} else endRun();
	}

	private void endRun() {
		toRun.setValue(new ArrayList<>());
		runFlag.setValue(NONE);
	}

	private List<Action> getChannelActions(int flag) {
		List list = new ArrayList<Action>();
		if (balanceActions.getValue() == null || balanceActions.getValue().size() == 0) return list;
		for (Action action : balanceActions.getValue()) {
			if (action.channel_id == flag)
				list.add(action);
		}
		return list;
	}

	public interface RunBalanceListener {
		void startRun(Action a, int index);
	}

	public boolean hasChannels() {
		return selectedChannels.getValue() != null && selectedChannels.getValue().size() > 0;
	}
}