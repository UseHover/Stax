package com.hover.stax.schedules;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.stax.actions.Action;
import com.hover.stax.database.DatabaseRepo;

public class ScheduleDetailViewModel extends AndroidViewModel {
	private final String TAG = "ScheduleViewModel";

	private DatabaseRepo repo;
	private MutableLiveData<Schedule> schedule;
	private LiveData<Action> action;

	public ScheduleDetailViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		schedule = new MutableLiveData<>();
		action = Transformations.switchMap(schedule, this::loadAction);
	}

	public void setSchedule(int id) {
		new Thread(() -> schedule.postValue(repo.getSchedule(id))).start();
	}

	public LiveData<Schedule> getSchedule() {
		if (schedule == null) { return new MutableLiveData<>(); }
		return schedule;
	}

	private LiveData<Action> loadAction(Schedule s) {
		if (s != null) { return repo.getLiveAction(s.action_id); }
		return new MutableLiveData<>();
	}

	public LiveData<Action> getAction() {
		if (action == null) { action = new MutableLiveData<>(); }
		return action;
	}

	void deleteSchedule() {
		repo.delete(schedule.getValue());
	}
}
