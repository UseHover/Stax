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

	protected LiveData<List<StaxContact>> recentContacts = new MutableLiveData<>();
	protected MutableLiveData<Schedule> schedule = new MutableLiveData<>();
	protected MutableLiveData<Boolean> isEditing = new MutableLiveData<>();

	public StagedViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);

		isEditing.setValue(true);
		recentContacts = repo.getAllContacts();
	}

	public void setEditing(boolean isEdit) { isEditing.setValue(isEdit); }
	public LiveData<Boolean> getIsEditing() {
		if (isEditing == null) {
			isEditing = new MutableLiveData<>();
			isEditing.setValue(false);
		}
		return isEditing;
	}

	public LiveData<List<StaxContact>> getRecentContacts() {
		if (recentContacts == null) { recentContacts = new MutableLiveData<>(); }
		return recentContacts;
	}

	protected void saveSchedule(Schedule s) {
		Amplitude.getInstance().logEvent(getApplication().getString(R.string.scheduled_complete, s.type));
		repo.insert(s);
	}
}
