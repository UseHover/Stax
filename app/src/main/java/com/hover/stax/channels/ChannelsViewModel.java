package com.hover.stax.channels;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessaging;
import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.api.Hover;
import com.hover.sdk.sims.SimInfo;
import com.hover.stax.R;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.pushNotification.PushNotificationTopicsInterface;
import com.hover.stax.requests.Request;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static org.koin.java.KoinJavaComponent.get;

public class ChannelsViewModel extends AndroidViewModel implements ChannelDropdown.HighlightListener, PushNotificationTopicsInterface {
    public final static String TAG = "ChannelDropdownVM";

    private DatabaseRepo repo = get(DatabaseRepo.class);
    private MutableLiveData<String> type = new MutableLiveData<>();

    private MutableLiveData<List<SimInfo>> sims;
    private LiveData<List<String>> simHniList = new MutableLiveData<>();

    private LiveData<List<Channel>> allChannels;
    private LiveData<List<Channel>> selectedChannels;
    private MediatorLiveData<List<Channel>> simChannels;
    private MediatorLiveData<Channel> activeChannel = new MediatorLiveData<>();
    private MediatorLiveData<List<HoverAction>> channelActions = new MediatorLiveData<>();

    public ChannelsViewModel(Application application) {
        super(application);
        type.setValue(HoverAction.BALANCE);

        loadChannels();
        loadSims();

        simHniList = Transformations.map(sims, this::getHnisAndSubscribeToEachOnFirebase);

        simChannels = new MediatorLiveData<>();
        simChannels.addSource(allChannels, this::onChannelsUpdateHnis);
        simChannels.addSource(simHniList, this::onSimUpdate);

        activeChannel.addSource(selectedChannels, this::setActiveChannelIfNull);

        channelActions.addSource(type, this::loadActions);
        channelActions.addSource(selectedChannels, this::loadActions);
        channelActions.addSource(activeChannel, this::loadActions);
    }

    public void setType(String t) {
        type.setValue(t);
    }

    public String getType() {
        return type.getValue();
    }

    private void loadChannels() {
        if (allChannels == null) {
            allChannels = new MutableLiveData<>();
        }
        if (selectedChannels == null) {
            selectedChannels = new MutableLiveData<>();
        }
        allChannels = repo.getAllChannels();
        selectedChannels = repo.getSelected();
    }

    public LiveData<List<Channel>> getAllChannels() {
        if (allChannels == null) {
            allChannels = new MutableLiveData<>();
        }
        return allChannels;
    }

    public LiveData<List<Channel>> getSelectedChannels() {
        if (selectedChannels == null) {
            selectedChannels = new MutableLiveData<>();
        }
        return selectedChannels;
    }

    void loadSims() {
        if (sims == null) {
            sims = new MutableLiveData<>();
        }
        new Thread(() -> sims.postValue(repo.getPresentSims())).start();
        LocalBroadcastManager.getInstance(getApplication())
                .registerReceiver(simReceiver, new IntentFilter(Utils.getPackage(getApplication()) + ".NEW_SIM_INFO_ACTION"));
        Hover.updateSimInfo(getApplication());
    }

    private final BroadcastReceiver simReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new Thread(() -> sims.postValue(repo.getPresentSims())).start();
        }
    };

    public LiveData<List<SimInfo>> getSims() {
        if (sims == null) {
            sims = new MutableLiveData<>();
        }
        return sims;
    }

    public LiveData<List<String>> getSimHniList() {
        if (simHniList == null) {
            simHniList = new MutableLiveData<>();
        }
        return simHniList;
    }

    private List<String> getHnisAndSubscribeToEachOnFirebase(List<SimInfo> sims) {
        if (sims == null) return null;
        List<String> hniList = new ArrayList<>();
        for (SimInfo sim : sims) {
            if (!hniList.contains(sim.getOSReportedHni())) {
                FirebaseMessaging.getInstance().subscribeToTopic("sim-" + sim.getOSReportedHni());
                FirebaseMessaging.getInstance().subscribeToTopic(sim.getCountryIso().toUpperCase());
                hniList.add(sim.getOSReportedHni());
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
            for (Channel c : channels)
                if (c.defaultAccount) {
                    activeChannel.postValue(c);
                }
        }
    }

    private void setActiveChannel(Channel channel) {
        activeChannel.setValue(channel);
    }

    void setActiveChannel(List<HoverAction> acts) {
        if (acts == null || acts.size() == 0) {
            return;
        }
        activeChannel.removeSource(channelActions);
        new Thread(() -> activeChannel.postValue(repo.getChannel(acts.get(0).channel_id))).start();
    }

    public LiveData<Channel> getActiveChannel() {
        return activeChannel;
    }

    @Override
    public void highlightChannel(Channel c) {
        setActiveChannel(c);
    }

    public void loadActions(String t) {
        if ((t.equals(HoverAction.BALANCE) && selectedChannels.getValue() == null) || (!t.equals(HoverAction.BALANCE) && activeChannel.getValue() == null))
            return;
        if (t.equals(HoverAction.BALANCE))
            loadActions(selectedChannels.getValue(), t);
        else
            loadActions(activeChannel.getValue(), t);
    }

    public void loadActions(Channel channel) {
        loadActions(channel, type.getValue());
    }

    private void loadActions(Channel c, String t) {
        new Thread(() -> channelActions.postValue(t.equals(HoverAction.P2P) ? repo.getTransferActions(c.id) : repo.getActions(c.id, t))).start();
    }

    public void loadActions(List<Channel> channels) {
        if (type.getValue().equals(HoverAction.BALANCE))
            loadActions(channels, type.getValue());
    }

    public void loadActions(List<Channel> channels, String t) {
        int[] ids = new int[channels.size()];
        for (int c = 0; c < channels.size(); c++)
            ids[c] = channels.get(c).id;
        new Thread(() -> channelActions.postValue(repo.getActions(ids, t))).start();
    }

    public LiveData<List<HoverAction>> getChannelActions() {
        return channelActions;
    }

    public void setChannelsSelected(List<Channel> channels) {
        if (channels == null || channels.isEmpty()) return;

        for (int i = 0; i < channels.size(); i++) {
            Channel c = channels.get(i);
            logChoice(c);
            c.selected = true;
            c.defaultAccount = (selectedChannels.getValue() == null || selectedChannels.getValue().size() == 0) && i == 0;
            repo.update(c);
        }
    }

    private void logChoice(Channel channel) {
        Timber.i("saving selected channel: %s", channel);
        joinChannelGroup(channel.id, getApplication().getApplicationContext());
        JSONObject args = new JSONObject();
        try {
            args.put(getApplication().getString(R.string.added_channel_id), channel.id);
        } catch (JSONException ignored) {
        }
        Utils.logAnalyticsEvent(getApplication().getString(R.string.new_channel_selected), args, getApplication().getBaseContext());
    }

    public String errorCheck() {
        if (activeChannel.getValue() == null)
            return getApplication().getString(R.string.channels_error_noselect);
        else if (channelActions.getValue() == null || channelActions.getValue().size() == 0) {
            return getApplication().getString(R.string.no_actions_fielderror, HoverAction.getHumanFriendlyType(getApplication(), type.getValue()));
        } else return null;
    }

    public void setChannelFromRequest(Request r) {
        if (r != null && selectedChannels.getValue() != null && selectedChannels.getValue().size() > 0) {
            new Thread(() -> {
                List<HoverAction> acts = repo.getActions(getChannelIds(selectedChannels.getValue()), r.requester_institution_id);
                if (acts.size() <= 0)
                    acts = repo.getActions(getChannelIds(simChannels.getValue()), r.requester_institution_id);
                if (acts.size() > 0)
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