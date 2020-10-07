package com.hover.stax.home;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.transactions.StaxTransaction;
import com.hover.stax.utils.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class BalancesViewModel extends AndroidViewModel {
	private final String TAG = "BalancesViewModel";

	private final int ALL = -1, NONE = 0;

	private DatabaseRepo repo;
	private RunBalanceListener listener;

	private LiveData<List<Channel>> selectedChannels;
	private LiveData<List<Action>> balanceActions;
	private MutableLiveData<Integer> runFlag;
	private MediatorLiveData<List<Action>> toRun;

	public BalancesViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		if (selectedChannels == null) {
			selectedChannels = new MutableLiveData<>();
		}
		if (runFlag == null) {
			runFlag = new MutableLiveData<>();
			runFlag.setValue(NONE);
		}

		selectedChannels = repo.getSelected();
		balanceActions = Transformations.switchMap(selectedChannels, this::loadBalanceActions);

		toRun = new MediatorLiveData<>();
		toRun.addSource(runFlag, this::onSetRunning);
		toRun.addSource(balanceActions, this::onSetBalanceActions);
	}

	void setListener(RunBalanceListener l) {
		listener = l;
	}

	public LiveData<List<Action>> getToRun() { return toRun; }

	public LiveData<List<Channel>> getSelectedChannels() {
		if (selectedChannels == null) {
			selectedChannels = new MutableLiveData<>();
		}
		return selectedChannels;
	}

	public LiveData<List<Action>> loadBalanceActions(List<Channel> channelList) {
		int[] ids = new int[channelList.size()];
		for (int c = 0; c < channelList.size(); c++) {
			ids[c] = channelList.get(c).id;
		}
		return repo.getLiveActions(ids, Action.BALANCE);
	}

	public LiveData<List<Action>> getBalanceActions() {
		return balanceActions;
	}

	public Channel getChannel(int id) {
		List<Channel> allChannels = selectedChannels.getValue() != null ? selectedChannels.getValue() : new ArrayList<>();
		for (Channel channel : allChannels) {
			if (channel.id == id) {
				return channel;
			}
		}
		return null;
	}

	void setRunning(int channel_id) {
		runFlag.setValue(channel_id);
	}

	void setRunning() {
		runFlag.setValue(ALL);
	}

	private void onSetRunning(Integer flag) {
		if (flag == null) return;
		if (flag == NONE) {
			toRun.setValue(new ArrayList<>());
		} else if (flag == ALL) startRun(balanceActions.getValue());
		else startRun(getChannelActions(flag));
	}

	private void onSetBalanceActions(List<Action> balanceActions) {
		if (runFlag.getValue() == null) return;
		if (runFlag.getValue() == ALL) startRun(balanceActions);
		else if (runFlag.getValue() != NONE) startRun(getChannelActions(runFlag.getValue()));
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

	void startRun(List<Action> actions) {
		if (actions == null || actions.size() == 0) return;
		toRun.setValue(actions);
		runNext(actions, 0);
	}

	private void runNext(List<Action> actions, int index) {
		if (listener != null)
			listener.startRun(actions.get(index), index);
		else
			UIHelper.flashMessage(getApplication(), "Failed to start run, please try again.");
	}

	void setRan(int index) {
		if (toRun.getValue().size() > index + 1) {
			runNext(toRun.getValue(), index + 1);
		} else {
			toRun.setValue(new ArrayList<>());
			runFlag.setValue(NONE);
		}
	}

	public interface RunBalanceListener {
		void startRun(Action a, int index);
	}
}