package com.hover.stax.requests;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.database.DatabaseRepo;

import java.util.ArrayList;
import java.util.List;

public class RequestViewModel extends AndroidViewModel {

	private DatabaseRepo repo;
	private MutableLiveData<List<Request>> requestList;
	private List<Request> rqL;
	private MutableLiveData<RequestStage> requestStage;
	private int addCounter = 0;
	public RequestViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		rqL = new ArrayList<>();
		rqL.add(new Request(addCounter));

		requestList = new MutableLiveData<>();
		requestStage = new MutableLiveData<>();
		requestStage.setValue(RequestStage.ENTER_RECIPIENT);

		requestList.setValue(rqL);

	}

	public void updateStage(RequestStage stage) {
		requestStage.postValue(stage);
	}


	void addRequest() {
		addCounter = addCounter + 1;
		rqL.add(addCounter, new Request(addCounter));
		requestList.postValue(rqL);
	}

	void updateRequestRecipient(int tag, String recipient) {
		Request request = rqL.get(tag);
		request.recipient = recipient;
		requestList.postValue(rqL);
	}

	void updateRequestRecipientNoUISync(int tag, String recipient) {
		Request request = rqL.get(tag);
		request.recipient = recipient;
	}

	void updateRequestAmount(String amount) {
		for(Request request : rqL) {
			request.amount = amount;
		}
	}
	void updateRequestMessage(String message) {
		for(Request request : rqL) {
			request.message = message;
		}
	}

	void setRequestStage(RequestStage stage) {requestStage.postValue(stage);}
	LiveData<RequestStage> getStage() {return requestStage;}
	LiveData<List<Request>> getIntendingRequests() {return requestList;}
}
