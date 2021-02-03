package com.hover.stax.requests;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.database.Constants;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.StagedViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewRequestViewModel extends StagedViewModel {
	public final static String TAG = "NewRequestViewModel";

	private MediatorLiveData<Channel> activeChannel = new MediatorLiveData<>();

	private MutableLiveData<String> amount = new MutableLiveData<>();
	private MutableLiveData<List<StaxContact>> requestees = new MutableLiveData<>();
	private MediatorLiveData<String> requesterNumber = new MediatorLiveData<>();
	private MutableLiveData<String> note = new MutableLiveData<>();

	private MediatorLiveData<Integer> amountError = new MediatorLiveData<>();
	private MediatorLiveData<Integer> requesteeError = new MediatorLiveData<>();
	private MediatorLiveData<Integer> requesterAccountError = new MediatorLiveData<>();
	private MediatorLiveData<Integer> requesterNumberError = new MediatorLiveData<>();

	private MutableLiveData<Request> formulatedRequest = new MutableLiveData<>();
	private MutableLiveData<List<Request>> finalRequests = new MutableLiveData<>();

	public NewRequestViewModel(@NonNull Application application) {
		super(application);
		requestees.setValue(new ArrayList<>(Collections.singletonList(new StaxContact(""))));
		formulatedRequest.setValue(null);

		requesterNumber.addSource(activeChannel, this::setRequesterNumber);

		amountError.addSource(amount, amount -> { if (amount != null && !amount.isEmpty() && !amount.equals("0")) amountError.setValue(null); });
		requesteeError.addSource(requestees, contacts -> { if (contacts != null && contacts.size() > 0) requesteeError.setValue(null); });
		requesterAccountError.addSource(activeChannel, channel -> { if (channel != null) requesterAccountError.setValue(null); });
		requesterNumberError.addSource(requesterNumber, number -> { if (number != null) requesterNumberError.setValue(null); });
	}

	void setAmount(String a) {
		amount.postValue(a);
	}

	LiveData<String> getAmount() {
		if (amount == null) {
			amount = new MutableLiveData<>();
		}
		return amount;
	}

	void setActiveChannel(Channel c) {
		activeChannel.setValue(c);
		setRequesterNumber(c);
	}
	LiveData<Channel> getActiveChannel() {
		return activeChannel;
	}

	void setRequesterNumber(Channel c) {
		if (c != null && c.accountNo != null && !c.accountNo.isEmpty())
			requesterNumber.setValue(c.accountNo);
		else if (c != null && requesterNumber.getValue() != null && !requesterNumber.getValue().isEmpty())
			c.accountNo = requesterNumber.getValue();
	}

	void setRequesterNumber(String number) {
		requesterNumber.postValue(number);
		if (activeChannel.getValue() != null)
			activeChannel.getValue().accountNo = number;
	}
	LiveData<String> getRequesterNumber() {
		if (requesterNumber == null) { requesterNumber = new MediatorLiveData<>(); }
		return requesterNumber;
	}

	public void onUpdate(int pos, StaxContact contact) {
		List<StaxContact> cs = requestees.getValue() != null ? requestees.getValue() : new ArrayList<>();
		cs.set(pos, contact);
		requestees.postValue(cs);
	}

	void addRecipient(StaxContact contact) {
		List<StaxContact> rList = requestees.getValue() != null ? requestees.getValue() : new ArrayList<>();
		rList.add(contact);
		requestees.postValue(rList);
	}

	LiveData<List<StaxContact>> getRequestees() {
		if (requestees == null) {
			requestees = new MutableLiveData<>();
		}
		return requestees;
	}

	void resetRecipients() { requestees.setValue(new ArrayList<>()); }

	LiveData<Integer> getAmountError() {
		if (amountError == null) { amountError = new MediatorLiveData<>(); }
		return amountError;
	}

	LiveData<Integer> getRequesteeError() {
		if (requesteeError == null) {
			requesteeError = new MediatorLiveData<>();
		}
		return requesteeError;
	}

	LiveData<Integer> getRequesterAccountError() {
		if(requesterAccountError == null) {
			requesterAccountError = new MediatorLiveData<>();
		}
		return requesterAccountError;
	}
	LiveData<Integer> getRequesterNumberError() {
		if(requesterNumberError == null) {
			requesterNumberError = new MediatorLiveData<>();
		}
		return requesterNumberError;
	}

	void setNote(String n) {
		note.postValue(n);
	}

	LiveData<String> getNote() {
		if (note == null) {
			note = new MutableLiveData<>();
			note.setValue(" ");
		}
		return note;
	}

	protected boolean validates() {
		boolean valid = true;
		if (!validNumber()) {
			valid = false;
			requesterNumberError.setValue(R.string.requester_number_fielderror);
		}
		if (!validAccount()) {
			valid = false;
			requesterAccountError.setValue(R.string.requester_account_error);
		}
		if (!validRequestees()) {
			valid = false;
			requesteeError.setValue(R.string.request_error_recipient);
		}
		return valid;
	}

	boolean validAmount() { return (amount.getValue() != null && !amount.getValue().isEmpty() && amount.getValue().matches("\\d+") && !amount.getValue().matches("[0]+")); }

	boolean validRequestees() { return (requestees.getValue() != null && requestees.getValue().size() > 0 && !requestees.getValue().get(0).getPhoneNumber().isEmpty()); }

	boolean validAccount() { return (activeChannel.getValue() != null); }

	boolean validNumber() { return (requesterNumber.getValue() != null && !requesterNumber.getValue().isEmpty()); }

	boolean validNote() { return (note.getValue() != null && !note.getValue().isEmpty()); }

	public void setSchedule(Schedule s) {
		schedule.setValue(s);
		setAmount(s.amount);
		new Thread(() -> requestees.postValue(repo.getContacts(s.recipient_ids.split(",")))).start();
		setNote(s.note);
	}

	public void createRequest() {
		repo.update(activeChannel.getValue());
		saveContacts();
		formulatedRequest.setValue(new Request(amount.getValue(), note.getValue(), requesterNumber.getValue(), activeChannel.getValue().institutionId));
	}

	LiveData<Request> getRequest() {
		if (formulatedRequest == null) { formulatedRequest = new MutableLiveData<>(); }
		return formulatedRequest;
	}

	void saveRequest() {
		ArrayList<Request> requests	= new ArrayList<>();
		for (StaxContact recipient : requestees.getValue()) {
			Request r = new Request(formulatedRequest.getValue(), recipient, getApplication());
			requests.add(r);
			repo.insert(r);
		}
		finalRequests.setValue(requests.size() > 0 ? requests : null);
	}

	LiveData<List<Request>> getRequests() {
		if (finalRequests == null) { finalRequests = new MutableLiveData<>(); }
		return finalRequests;
	}


	void removeInvalidRequestees() {
		if (requestees.getValue() != null && requestees.getValue().size() > 0) {
			List<StaxContact> contacts = new ArrayList<>();
			for (StaxContact c: requestees.getValue()) {
				if (c.phoneNumber != null && !c.phoneNumber.isEmpty())
					contacts.add(c);
			}
			requestees.setValue(contacts);
		}
	}

	public void saveContacts() {
		if (requestees.getValue() != null) {
			new Thread(() -> {
				for (StaxContact c: requestees.getValue()) {
					c.lastUsedTimestamp = DateUtils.now();
					repo.insertOrUpdate(c);
				}
			}).start();
		}
	}
}
