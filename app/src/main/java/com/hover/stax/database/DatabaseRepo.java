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
	private LiveData<List<Institution>> selectedInstitutions;

	public DatabaseRepo(Application application) {
		AppDatabase db = AppDatabase.getInstance(application);
		institutionDao = db.institutionDao();
		allInstitutions = institutionDao.getAll();
		selectedInstitutions = institutionDao.getSelected(true);
	}

	// Room executes all queries on a separate thread.
	// Observed LiveData will notify the observer when the data has changed.
	public LiveData<List<Institution>> getAll() {
		return allInstitutions;
	}

	public LiveData<List<Institution>> getSelected() {
		return selectedInstitutions;
	}

	void insert(Institution institution) {
		AppDatabase.databaseWriteExecutor.execute(() -> {
			institutionDao.insert(institution);
		});
	}
}
