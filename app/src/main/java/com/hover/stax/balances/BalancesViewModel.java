package com.hover.stax.balances;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.sdk.actions.HoverAction;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BalancesViewModel extends AndroidViewModel {
    private final String TAG = "BalancesViewModel";

    private final int ALL = -1, NONE = 0;

    private DatabaseRepo repo;
    private RunBalanceListener listener;
    private List<Integer> hasRunList = new ArrayList<>();
    private boolean hasActive = false;

    private LiveData<List<Channel>> selectedChannels;
    private LiveData<Channel> activeChannel;
    private LiveData<List<HoverAction>> actions = new MediatorLiveData<>();

    private MutableLiveData<Integer> runFlag = new MutableLiveData<>();
    private MediatorLiveData<List<HoverAction>> toRun;
    private MutableLiveData<Boolean> runBalanceError = new MutableLiveData<>();

    public BalancesViewModel(Application application) {
        super(application);
        repo = new DatabaseRepo(application);
        if (runFlag == null) runFlag.setValue(NONE);

        selectedChannels = repo.getSelected();
        actions = Transformations.switchMap(selectedChannels, this::loadActions);

        toRun = new MediatorLiveData<>();
        toRun.setValue(new ArrayList<>());
        toRun.addSource(runFlag, this::onSetRunning);
        toRun.addSource(actions, this::onActionsLoaded);
        runBalanceError.setValue(false);
    }

    public void setListener(RunBalanceListener l) {
        listener = l;
    }

    public LiveData<List<HoverAction>> getToRun() {
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

    public LiveData<List<HoverAction>> loadActions(List<Channel> channelList) {
        Log.e(TAG, "attempting to load " + channelList.size() + " balance actions");
        int[] ids = new int[channelList.size()];
        for (int c = 0; c < channelList.size(); c++)
            ids[c] = channelList.get(c).id;
        Log.e(TAG, "attempting to load balance actions for channels: " + Arrays.toString(ids));
        return repo.getLiveActions(ids, HoverAction.BALANCE);
    }

    public LiveData<List<HoverAction>> getActions() {
        return actions;
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
        Utils.logAnalyticsEvent(c.getString(R.string.refresh_balance_all), c);
        runFlag.setValue(ALL);
    }

    private void onSetRunning(Integer flag) {
        if (flag == null || flag == NONE) toRun.setValue(new ArrayList<>());
        else if (flag == ALL) startRun(actions.getValue());
        else startRun(getChannelActions(flag));
    }

    private void onActionsLoaded(List<HoverAction> actions) {
        if (runFlag.getValue() == null || toRun.getValue().size() > 0) return;
        if (runFlag.getValue() == ALL) startRun(actions);
        else if (runFlag.getValue() != NONE) startRun(getChannelActions(runFlag.getValue()));
    }

    void startRun(List<HoverAction> actions) {
        if (actions == null || actions.size() == 0) return;
        toRun.setValue(actions);
        runNext(actions, 0);
    }

    private void runNext(List<HoverAction> actions, int index) {
        if (listener != null && !hasActive) {
            hasActive = true;
            listener.startRun(actions.get(index), index);
        } else if (!hasActive)
            UIHelper.flashMessage(getApplication(), "Failed to start run, please try again.");
    }

    public void setRan(int index) {
        hasActive = false;
        if (toRun.getValue().size() > index + 1) {
            hasRunList.add(toRun.getValue().get(index).id);
            while (hasRunList.contains(toRun.getValue().get(index + 1).id))
                index = index + 1;
            if (toRun.getValue().size() > index + 1)
                runNext(toRun.getValue(), index + 1);
            else endRun();
        } else endRun();
    }

    private void endRun() {
        toRun.setValue(new ArrayList<>());
        runFlag.setValue(NONE);
        hasRunList = new ArrayList<>();
    }

    private List<HoverAction> getChannelActions(int flag) {
        List<HoverAction> list = new ArrayList<>();
        if (actions.getValue() == null || actions.getValue().size() == 0) return list;

        for (HoverAction action : actions.getValue()) {
            if (action.channel_id == flag)
                list.add(action);
        }
        return list;
    }

    public interface RunBalanceListener {
        void startRun(HoverAction a, int index);
    }

    public boolean hasChannels() {
        return selectedChannels.getValue() != null && selectedChannels.getValue().size() > 0;
    }
}