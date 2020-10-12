package com.hover.stax.requests;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.database.DatabaseRepo;

public class RequestDetailViewModel extends AndroidViewModel {
	private final String TAG = "RequestDetailViewModel";

	private DatabaseRepo repo;
	private MutableLiveData<Request> request;

	public RequestDetailViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		request = new MutableLiveData<>();
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

	void deleteRequest() {
		repo.delete(request.getValue());
	}
}
