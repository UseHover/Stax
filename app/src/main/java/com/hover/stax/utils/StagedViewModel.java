package com.hover.stax.utils;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.database.DatabaseRepo;

public abstract class StagedViewModel extends AndroidViewModel {

	protected DatabaseRepo repo;
	protected MutableLiveData<StagedEnum> stage = new MutableLiveData<>();

	protected MutableLiveData<Boolean> futureDated = new MutableLiveData<>();
	protected MutableLiveData<Long> futureDate = new MutableLiveData<>();

	public StagedViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		futureDated.setValue(false);
		futureDate.setValue(null);
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

	public boolean goToStage(StagedEnum s) {
		if (stage == null) return false;
		stage.postValue(s);
		return true;
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
	}

	public LiveData<Long> getFutureDate() {
		if (futureDate == null) {
			futureDate = new MutableLiveData<>();
		}
		return futureDate;
	}

	public interface StagedEnum {
		StagedEnum next();
		StagedEnum prev();
		int compare(StagedEnum e);
	}
}
