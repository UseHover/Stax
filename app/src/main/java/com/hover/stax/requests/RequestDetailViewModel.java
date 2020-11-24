package com.hover.stax.requests;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.stax.contacts.StaxContact;
import com.hover.stax.database.DatabaseRepo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RequestDetailViewModel extends AndroidViewModel {
	private final String TAG = "RequestDetailViewModel";

	private DatabaseRepo repo;
	private MutableLiveData<Request> request;
	private LiveData<List<StaxContact>> recipients = new MutableLiveData<>();

	public RequestDetailViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		request = new MutableLiveData<>();
		recipients = Transformations.switchMap(request, this::loadRecipients);
	}

	public void setRequest(int id) {
		new Thread(() -> request.postValue(repo.getRequest(id))).start();
	}

	public LiveData<Request> getRequest() {
		if (request == null) {
			return new MutableLiveData<>();
		}
		return request;
	}

	public LiveData<List<StaxContact>> loadRecipients(Request r) {
		return repo.getLiveContacts(r.recipient_ids.split(","));
	}

	public LiveData<List<StaxContact>> getRecipients() {
		if (recipients == null) { return new MutableLiveData<>(); }
		return recipients;
	}

	void deleteRequest() {
		repo.delete(request.getValue());
	}
}
