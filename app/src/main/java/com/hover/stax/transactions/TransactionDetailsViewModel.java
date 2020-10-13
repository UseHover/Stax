package com.hover.stax.transactions;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.sdk.api.Hover;
import com.hover.sdk.sms.MessageLog;
import com.hover.sdk.transactions.Transaction;
import com.hover.stax.actions.Action;
import com.hover.stax.database.DatabaseRepo;

import java.util.ArrayList;
import java.util.List;

public class TransactionDetailsViewModel extends AndroidViewModel {
	private final String TAG = "TDViewModel";

	private DatabaseRepo repo;

	private MutableLiveData<StaxTransaction> transaction;
	private LiveData<Action> action;
	private MediatorLiveData<List<UssdCallResponse>> messages;
	private LiveData<List<UssdCallResponse>> sms;

	public TransactionDetailsViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		transaction = new MutableLiveData<>();
		action = new MutableLiveData<>();
		messages = new MediatorLiveData<>();

		action = Transformations.switchMap(transaction, t -> repo.getLiveAction(t.action_id));
		messages.addSource(transaction, this::loadMessages);
		messages.addSource(action, this::loadMessages);
		sms = Transformations.map(transaction, this::loadSms);
	}

	void setTransaction(String uuid) {
		new Thread(() -> transaction.postValue(repo.getTransaction(uuid))).start();
	}

	LiveData<StaxTransaction> getTransaction() {
		return transaction;
	}

	LiveData<Action> getAction() {
		if (action == null) {
			action = new MutableLiveData<>();
		}
		return action;
	}

	void loadMessages(StaxTransaction t) {
		if (action.getValue() != null && t != null) loadMessages(t, action.getValue());
	}

	void loadMessages(Action a) {
		if (transaction.getValue() != null && a != null) loadMessages(transaction.getValue(), a);
	}

	void loadMessages(StaxTransaction t, Action a) {
		List<UssdCallResponse> ussds = UssdCallResponse.generateConvo(Hover.getTransaction(t.uuid, getApplication()), a);
		messages.setValue(ussds);
	}

	LiveData<List<UssdCallResponse>> getMessages() {
		if (messages == null) {
			messages = new MediatorLiveData<>();
		}
		return messages;
	}

	List<UssdCallResponse> loadSms(StaxTransaction t) {
		if (t == null) return null;
		Transaction transaction = Hover.getTransaction(t.uuid, getApplication());
		if (transaction.smsHits == null) return null;
		List<UssdCallResponse> smses = new ArrayList<>();
		for (int i = 0; i < transaction.smsHits.length(); i++) {
			MessageLog sms = getSMSMessageByUUID(transaction.smsHits.optString(i));
			smses.add(new UssdCallResponse(null, sms.msg));
		}
		return smses;
	}

	LiveData<List<UssdCallResponse>> getSms() {
		if (messages == null) {
			messages = new MediatorLiveData<>();
		}
		return sms;
	}

	private MessageLog getSMSMessageByUUID(String uuid) {
		return Hover.getSMSMessageByUUID(uuid, getApplication());
	}
}
