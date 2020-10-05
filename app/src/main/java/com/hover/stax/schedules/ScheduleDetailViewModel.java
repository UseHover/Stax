package com.hover.stax.schedules;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.hover.stax.database.DatabaseRepo;

public class ScheduleDetailViewModel extends AndroidViewModel {
	private final String TAG = "ScheduleViewModel";

	private DatabaseRepo repo;
	private MutableLiveData<Schedule> schedule;

	public ScheduleDetailViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		schedule = new MutableLiveData<>();
	}

	void setSchedule(int id) {
		new Thread(() -> schedule.postValue(repo.getSchedule(id))).start();
	}

	LiveData<Schedule> getSchedule() {
		if (schedule == null) { return new MutableLiveData<>(); }
		return schedule;
	}

	void deleteSchedule() {
		repo.delete(schedule.getValue());
	}
}
