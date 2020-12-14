package com.hover.stax.utils;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.schedules.Schedule;

import java.util.List;

public abstract class StagedViewModel extends AndroidViewModel {

	protected DatabaseRepo repo;
	protected MutableLiveData<StagedEnum> stage = new MutableLiveData<>();

	private LiveData<List<Channel>> selectedChannels = new MutableLiveData<>();
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

	public StagedViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		selectedChannels = repo.getSelected();
		activeChannel.addSource(selectedChannels, this::findActiveChannel);

		futureDated.setValue(false);
		futureDate.setValue(null);

		isRepeating.setValue(false);
		frequency.setValue(3);
		repeatSaved.setValue(false);
		endDate.setValue(null);
		isEditing.setValue(false);

		recentContacts = repo.getAllContacts();
	}

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

	private void findActiveChannel(List<Channel> channels) {
		if (channels != null && channels.size() > 0) {
			activeChannel.setValue(channels.get(0));
		}
	}

	public void setActiveChannel(int channel_id) {
		if (selectedChannels.getValue() == null || selectedChannels.getValue().size() == 0) {
			return;
		}
		activeChannel.setValue(getChannelById(channel_id));
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
