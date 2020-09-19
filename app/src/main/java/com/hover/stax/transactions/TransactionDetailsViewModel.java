package com.hover.stax.transactions;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.android.gms.common.util.ArrayUtils;
import com.hover.sdk.api.Hover;
import com.hover.sdk.sms.MessageLog;
import com.hover.sdk.transactions.Transaction;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.actions.Action;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.utils.Utils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class TransactionDetailsViewModel extends AndroidViewModel {
	private final String TAG = "TDViewModel";

	private DatabaseRepo repo;

	private MutableLiveData<StaxTransaction> transaction;
	private LiveData<Action> action;
	private LiveData<List<UssdCallResponse>> messages;
	private LiveData<List<UssdCallResponse>> sms;

	public TransactionDetailsViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		transaction = new MutableLiveData<>();
		action = new MutableLiveData<>();
		messages = new MediatorLiveData<>();

		action = Transformations.switchMap(transaction, this::loadAction);
		messages = Transformations.switchMap(action, this::loadMessages);
		sms = Transformations.switchMap(transaction, this::loadSms);
	}

	void setTransaction(String uuid) {
		new Thread(() -> transaction.postValue(repo.getTransaction(uuid))).start();
	}

	LiveData<StaxTransaction> getTransaction() { return transaction; }

	LiveData<Action> loadAction(StaxTransaction t) { return repo.getLiveAction(t.action_id); }

	LiveData<Action> getAction() { return action; }

	MutableLiveData<List<UssdCallResponse>> loadMessages(Action a) {
		if (transaction.getValue() == null || a == null) return null;
		List<UssdCallResponse> ussds = UssdCallResponse.generateConvo(Hover.getTransaction(transaction.getValue().uuid, getApplication()), a);
		MutableLiveData<List<UssdCallResponse>> liveData = new MutableLiveData<>();
		liveData.setValue(ussds);
		return liveData;
	}

	LiveData<List<UssdCallResponse>> getMessages() { return messages; }

	MutableLiveData<List<UssdCallResponse>> loadSms(StaxTransaction t) {
		if (t == null) return null;
		Transaction transaction = Hover.getTransaction(t.uuid, getApplication());
		if (transaction.smsHits == null) return null;
		List<UssdCallResponse> smses = new ArrayList<>();
		for (int i = 0; i < transaction.smsHits.length(); i++) {
			MessageLog sms = getSMSMessageByUUID(transaction.smsHits.optString(i));
			smses.add(new UssdCallResponse(null, sms.msg));
		}
		MutableLiveData<List<UssdCallResponse>> liveData = new MutableLiveData<>();
		liveData.setValue(smses);
		return liveData;
	}

	LiveData<List<UssdCallResponse>> getSms() { return sms; }

	private MessageLog getSMSMessageByUUID(String uuid) {
		return Hover.getSMSMessageByUUID(uuid, ApplicationInstance.getContext());
	}
}
