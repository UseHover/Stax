package com.hover.stax.utils;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.amplitude.api.Amplitude;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.sims.Sim;

import java.util.ArrayList;
import java.util.List;

public abstract class StagedViewModel extends AndroidViewModel {

	protected DatabaseRepo repo;
	protected MutableLiveData<StagedEnum> stage = new MutableLiveData<>();

	protected LiveData<List<Channel>> selectedChannels = new MutableLiveData<>();
	protected MediatorLiveData<Channel> activeChannel = new MediatorLiveData<>();

	protected MutableLiveData<Boolean> futureDated = new MutableLiveData<>();
	protected MutableLiveData<Long> futureDate = new MutableLiveData<>();

	protected MutableLiveData<Boolean> isRepeating = new MutableLiveData<>();
	protected MutableLiveData<Integer> frequency = new MutableLiveData<>();
	protected MutableLiveData<Long> endDate = new MutableLiveData<>();
	protected MutableLiveData<Integer> repeatTimes = new MutableLiveData<>();
	protected MutableLiveData<Boolean> repeatSaved = new MutableLiveData<>();

	protected LiveData<List<StaxContact>> recentContacts = new MutableLiveData<>();
	protected MutableLiveData<Schedule> schedule = new MutableLiveData<>();
	protected MutableLiveData<Boolean> isEditing = new MutableLiveData<>();

	private MediatorLiveData<List<Channel>> simChannels;
	private LiveData<List<String>> simHniList = new MutableLiveData<>();
	private MutableLiveData<List<Sim>> sims;

	public StagedViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		selectedChannels = repo.getAllChannelsBySelectedOrder();
		loadSims();
		simHniList = Transformations.map(sims, this::getHnisAndSubscribeToEachOnFirebase);

		simChannels = new MediatorLiveData<>();
		simChannels.addSource(selectedChannels, this::onChannelsUpdateHnis);
		simChannels.addSource(simHniList, this::onSimUpdate);

		//Prevent auto select
		//activeChannel.addSource(selectedChannels, this::setActiveChannelIfNull);

		futureDated.setValue(false);
		futureDate.setValue(null);

		isRepeating.setValue(false);
		frequency.setValue(3);
		repeatSaved.setValue(false);
		endDate.setValue(null);
		isEditing.setValue(false);

		recentContacts = repo.getAllContacts();
	}

	void loadSims() {
		if (sims == null) {
			sims = new MutableLiveData<>();
		}
		new Thread(() -> sims.postValue(repo.getSims())).start();
		LocalBroadcastManager.getInstance(getApplication())
				.registerReceiver(simReceiver, new IntentFilter(Utils.getPackage(getApplication()) + ".NEW_SIM_INFO_ACTION"));
	}
	private final BroadcastReceiver simReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			new Thread(() -> sims.postValue(repo.getSims())).start();
		}
	};

	public LiveData<StagedEnum> getStage() {
		return stage;
	}

	public void setStage(StagedEnum s) {
		stage.setValue(s);
	}

	public void goToNextStage() {
		StagedEnum next = stage.getValue().next();
		stage.postValue(next);
	}

	public void goToPrevStage() {
		StagedEnum prev = stage.getValue().prev();
		stage.postValue(prev);
	}

	private void onChannelsUpdateHnis(List<Channel> channels) {
		Channel.updateSimChannels(simChannels,channels, simHniList.getValue());
	}

	private void onSimUpdate(List<String> hniList) {
		Channel.updateSimChannels(simChannels,selectedChannels.getValue(), simHniList.getValue());
	}

	public LiveData<List<Channel>> getSimChannels() {
		return simChannels;
	}

	private List<String> getHnisAndSubscribeToEachOnFirebase(List<Sim> sims) {
		if (sims == null) return null;
		List<String> hniList = new ArrayList<>();
		for (Sim sim : sims) {
			if (!hniList.contains(sim.hni)) {
				FirebaseMessaging.getInstance().subscribeToTopic("sim-" + sim.hni);
				hniList.add(sim.hni);
			}
		}
		return hniList;
	}


	protected void setActiveChannelIfNull(List<Channel> channels) {
		if (channels != null && channels.size() > 0 && activeChannel.getValue() == null)
			activeChannel.setValue(channels.get(0));
	}

	public void setActiveChannel(int channel_id) {
		if (selectedChannels.getValue() == null || selectedChannels.getValue().size() == 0)
			return;
		activeChannel.setValue(getChannelById(channel_id));
	}
	public void setActiveChannel(Channel channel) {
		activeChannel.setValue(channel);
	}

	protected Channel getChannelById(int id) {
		if (selectedChannels.getValue() == null || selectedChannels.getValue().size() == 0) return null;
		for (Channel c : selectedChannels.getValue()) {
			if (c.id == id) return c;
		}
		return null;
	}

	public LiveData<Channel> getActiveChannel() {
		return activeChannel;
	}
	public LiveData<List<Channel>> getSelectedChannels() {
		return selectedChannels;
	}

	public void setIsFutureDated(boolean isFuture) {
		futureDated.setValue(isFuture);
	}

	public LiveData<Boolean> getIsFuture() {
		if (futureDated == null) {
			futureDated = new MutableLiveData<>();
			futureDated.setValue(false);
		}
		return futureDated;
	}

	public void setFutureDate(Long date) {
		futureDate.setValue(date);
		if (isRepeating.getValue() != null && isRepeating.getValue() && endDate.getValue() != null && frequency.getValue() != null)
			calculateRepeatTimes(endDate.getValue(), frequency.getValue());
	}

	public LiveData<Long> getFutureDate() {
		if (futureDate == null) {
			futureDate = new MutableLiveData<>();
		}
		return futureDate;
	}

	public void setIsRepeating(boolean repeat) {
		isRepeating.setValue(repeat);
	}

	public LiveData<Boolean> getIsRepeating() {
		if (isRepeating == null) {
			isRepeating = new MutableLiveData<>();
			isRepeating.setValue(false);
		}
		return isRepeating;
	}

	public void setFrequency(Integer freq) {
		frequency.setValue(freq);
		endDate.setValue(null);
		repeatTimes.setValue(null);
	}

	public LiveData<Integer> getFrequency() {
		if (frequency == null) { frequency = new MutableLiveData<>(); }
		return frequency;
	}

	public void setEndDate(Long date) {
		endDate.setValue(date);
		if (frequency.getValue() != null)
			calculateRepeatTimes(date, frequency.getValue());
	}

	public LiveData<Long> getEndDate() {
		if (endDate == null) { endDate = new MutableLiveData<>(); }
		return endDate;
	}

	public void setRepeatTimes(Integer times) {
		repeatTimes.setValue(times);
		if (times != null)
			calculateEndDate(times);
		else
			endDate.setValue(null);
	}

	public LiveData<Integer> getRepeatTimes() {
		if (repeatTimes == null) { repeatTimes = new MutableLiveData<>(); }
		return repeatTimes;
	}

	public void saveRepeat() { repeatSaved.setValue(true); }
	public LiveData<Boolean> repeatSaved() {
		if (repeatSaved == null) {
			repeatSaved = new MutableLiveData<>();
			repeatSaved.setValue(false);
		}
		return repeatSaved;
	}

	public void setEditing(boolean isEdit) { isEditing.setValue(isEdit); }
	public LiveData<Boolean> getIsEditing() {
		if (isEditing == null) {
			isEditing = new MutableLiveData<>();
			isEditing.setValue(false);
		}
		return isEditing;
	}

	private void calculateRepeatTimes(Long end_date, int freq) {
		switch (freq) {
			case 1: repeatTimes.setValue(DateUtils.getWeeks(getStartDate(), end_date)); break;
			case 2: repeatTimes.setValue(DateUtils.getBiweeks(getStartDate(), end_date)); break;
			case 3: repeatTimes.setValue(DateUtils.getMonths(getStartDate(), end_date)); break;
			default: repeatTimes.setValue(DateUtils.getDays(getStartDate(), end_date)); break;
		}
	}

	public Long getStartDate() {
		return futureDate.getValue() == null ? DateUtils.today() : futureDate.getValue();
	}

	private void calculateEndDate(int repeatTimes) {
		endDate.setValue(DateUtils.getDate(getStartDate(), frequency.getValue(), repeatTimes));
	}

	public LiveData<List<StaxContact>> getRecentContacts() {
		if (recentContacts == null) { recentContacts = new MutableLiveData<>(); }
		return recentContacts;
	}

	public interface StagedEnum {
		StagedEnum next();
		StagedEnum prev();
		int compare(StagedEnum e);
	}

	protected void saveSchedule(Schedule s) {
		Amplitude.getInstance().logEvent(getApplication().getString(R.string.scheduled_complete, s.type));
		repo.insert(s);
	}
}
