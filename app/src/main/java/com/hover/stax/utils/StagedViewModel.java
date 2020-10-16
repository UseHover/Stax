package com.hover.stax.utils;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.schedules.Schedule;

public abstract class StagedViewModel extends AndroidViewModel {

	protected DatabaseRepo repo;
	protected MutableLiveData<StagedEnum> stage = new MutableLiveData<>();

	protected MutableLiveData<Boolean> futureDated = new MutableLiveData<>();
	protected MutableLiveData<Long> futureDate = new MutableLiveData<>();

	protected MutableLiveData<Boolean> isRepeating = new MutableLiveData<>();
	protected MutableLiveData<Integer> frequency = new MutableLiveData<>();
	protected MutableLiveData<Long> endDate = new MutableLiveData<>();
	protected MutableLiveData<Integer> repeatTimes = new MutableLiveData<>();
	protected MutableLiveData<Boolean> repeatSaved = new MutableLiveData<>();

	protected MutableLiveData<Schedule> schedule = new MutableLiveData<>();

	public StagedViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		futureDated.setValue(false);
		futureDate.setValue(null);

		isRepeating.setValue(false);
		frequency.setValue(0);
		repeatSaved.setValue(false);
		endDate.setValue(null);
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
		if (endDate == null) {
			endDate = new MutableLiveData<>();
		}
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

	private void calculateRepeatTimes(Long end_date, int freq) {
		Long start = futureDate.getValue() == null ? DateUtils.now() : futureDate.getValue();
		switch (freq) {
			case 1: repeatTimes.setValue(DateUtils.getWeeks(start, end_date)); break;
			case 2: repeatTimes.setValue(DateUtils.getBiweeks(start, end_date)); break;
			case 3: repeatTimes.setValue(DateUtils.getMonths(start, end_date)); break;
			default: repeatTimes.setValue(DateUtils.getDays(start, end_date)); break;
		}
	}

	private void calculateEndDate(int repeatTimes) {
		Long start = futureDate.getValue() == null ? DateUtils.now() : futureDate.getValue();
		endDate.setValue(DateUtils.getDate(start, frequency.getValue(), repeatTimes));
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
