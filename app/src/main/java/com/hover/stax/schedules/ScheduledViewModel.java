package com.hover.stax.schedules;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.database.DatabaseRepo;

import java.util.List;

public class ScheduledViewModel extends AndroidViewModel {
	private final String TAG = "ScheduledViewModel";

	private DatabaseRepo repo;

	private LiveData<List<Schedule>> schedules;

	public ScheduledViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);

		schedules = new MutableLiveData<>();
		schedules = repo.getFutureTransactions();
	}

	public LiveData<List<Schedule>> getScheduled() { return schedules; }
}
