package com.hover.stax.requests;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.database.DatabaseRepo;

import java.util.ArrayList;
import java.util.List;

public class NewRequestViewModel extends AndroidViewModel {

	private DatabaseRepo repo;
	private MutableLiveData<RequestStage> requestStage = new MutableLiveData<>();

	private MutableLiveData<String> amount = new MutableLiveData<>();
	private MutableLiveData<List<String>> recipients = new MutableLiveData<>();
	private MutableLiveData<String> note = new MutableLiveData<>();
	private MutableLiveData<Boolean> futureDated = new MutableLiveData<>();
	private MutableLiveData<Long> futureDate = new MutableLiveData<>();

	public NewRequestViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);

		requestStage.setValue(RequestStage.RECIPIENT);
		recipients.setValue(new ArrayList<>());
		futureDated.setValue(false);
		futureDate.setValue(null);
	}

	LiveData<RequestStage> getStage() { return requestStage; }
	void setStage(RequestStage stage) { requestStage.setValue(stage); }
	void goToNextStage() {
		RequestStage next = requestStage.getValue() != null ? requestStage.getValue().next() : RequestStage.RECIPIENT;
		requestStage.postValue(next);
	}

	boolean goToStage(RequestStage stage) {
		if (stage == null) return false;
		requestStage.postValue(stage);
		return true;
	}

	void setAmount(String a) { amount.postValue(a); }
	LiveData<String> getAmount() {
		if (amount == null) { amount = new MutableLiveData<>(); }
		return amount;
	}

	void addRecipient(String recipient) {
		List<String> rList = recipients.getValue() != null ? recipients.getValue() : new ArrayList<>();
		rList.add(recipient);
		recipients.postValue(rList);
	}
	LiveData<List<String>> getRecipients() {
		if (recipients == null) { recipients = new MutableLiveData<>(); }
		return recipients;
	}

	void setNote(String n) { note.postValue(n); }
	LiveData<String> getNote() {
		if (note == null) { note = new MutableLiveData<>(); note.setValue(" "); }
		return note;
	}

	void setIsFutureDated(boolean isFuture) { futureDated.setValue(isFuture); }
	LiveData<Boolean> getIsFuture() {
		if (futureDated == null) { futureDated = new MutableLiveData<>(); futureDated.setValue(false);}
		return futureDated;
	}
	void setFutureDate(Long date) { futureDate.setValue(date); }
	LiveData<Long> getFutureDate() {
		if (futureDate == null) { futureDate = new MutableLiveData<>(); }
		return futureDate;
	}

	void saveToDatabase() {
		for (String recipient : recipients.getValue())
			repo.insert(new Request(recipient, amount.getValue(), note.getValue()));
	}
}
