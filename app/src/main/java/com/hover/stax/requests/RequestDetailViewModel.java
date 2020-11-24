package com.hover.stax.requests;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;

public class RequestDetailViewModel extends AndroidViewModel {
	private final String TAG = "RequestDetailViewModel";

	private DatabaseRepo repo;
	private MutableLiveData<Request> request;
	private MutableLiveData<String> channelName;

	public RequestDetailViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		request = new MutableLiveData<>();
		channelName = new MutableLiveData<>();
	}

	public void setRequest(int id) {
		new Thread(() ->{
			Request req = repo.getRequest(id);
			Channel channel = repo.getChannel(req.recipient_channel_id);

			request.postValue(req);
			channelName.postValue(channel.name);

		} ).start();
	}

	public LiveData<Request> getRequest() {
		if (request == null) {
			return new MutableLiveData<>();
		}
		return request;
	}

	public LiveData<String> getChannelName() {
		if(channelName == null) {
			return new MutableLiveData<>();
		}
		return channelName;
	}

	void deleteRequest() {
		repo.delete(request.getValue());
	}
}
