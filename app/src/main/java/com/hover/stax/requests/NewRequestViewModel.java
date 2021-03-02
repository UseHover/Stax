package com.hover.stax.requests;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.transfers.AbstractFormViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewRequestViewModel extends AbstractFormViewModel {
	public final static String TAG = "NewRequestViewModel";

	private MediatorLiveData<Channel> activeChannel = new MediatorLiveData<>();

	private MutableLiveData<String> amount = new MutableLiveData<>();
	private MutableLiveData<List<StaxContact>> requestees = new MutableLiveData<>();
	private MediatorLiveData<String> requesterNumber = new MediatorLiveData<>();
	private MutableLiveData<String> note = new MutableLiveData<>();

	private MutableLiveData<Request> formulatedRequest = new MutableLiveData<>();
	private MutableLiveData<List<Request>> finalRequests = new MutableLiveData<>();

	public NewRequestViewModel(@NonNull Application application) {
		super(application);
		requestees.setValue(new ArrayList<>(Collections.singletonList(new StaxContact(""))));
		formulatedRequest.setValue(null);

		requesterNumber.addSource(activeChannel, this::setRequesterNumber);
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
	}
	LiveData<Channel> getActiveChannel() {
		return activeChannel;
	}

	void setRequesterNumber(Channel c) {
		if (c != null) requesterNumber.setValue(c.accountNo);
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
		try { cs.set(pos, contact);
		} catch (IndexOutOfBoundsException e) { cs.add(contact); }
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

	boolean validAmount() { return (amount.getValue() != null && !amount.getValue().isEmpty() && amount.getValue().matches("\\d+") && !amount.getValue().matches("[0]+")); }

	String requesteeErrors() {
		if (requestees.getValue() != null && requestees.getValue().size() > 0 && !requestees.getValue().get(0).getPhoneNumber().isEmpty())
			return null;
		return getApplication().getString(R.string.request_error_recipient);
	}

	String requesterAcctNoError() {
		if (requesterNumber.getValue() == null && !requesterNumber.getValue().isEmpty())
			return null;
		return getApplication().getString(R.string.requester_number_fielderror);
	}

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
		if (finalRequests.getValue() != null && finalRequests.getValue().size() > 0 && requestees.getValue() != null && finalRequests.getValue().size() == requestees.getValue().size())
			return;
		ArrayList<Request> requests = new ArrayList<>();
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
