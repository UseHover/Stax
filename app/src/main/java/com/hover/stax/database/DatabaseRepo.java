package com.hover.stax.database;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;

import com.hover.stax.institutions.Institution;
import com.hover.stax.institutions.InstitutionDao;

import java.util.List;

public class DatabaseRepo {
	private InstitutionDao institutionDao;
	private LiveData<List<Institution>> allInstitutions;

	public DatabaseRepo(Application application) {
		AppDatabase db = AppDatabase.getInstance(application);
		institutionDao = db.institutionDao();
		allInstitutions = institutionDao.getAll();
	}

	// Room executes all queries on a separate thread.
	// Observed LiveData will notify the observer when the data has changed.
	public LiveData<List<Institution>> getAll() {
		return allInstitutions;
	}

	void insert(Institution institution) {
		AppDatabase.databaseWriteExecutor.execute(() -> {
			institutionDao.insert(institution);
		});
	}
}
