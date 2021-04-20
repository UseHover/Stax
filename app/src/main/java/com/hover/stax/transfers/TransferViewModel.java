package com.hover.stax.transfers;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.i18n.phonenumbers.NumberParseException;
import com.hover.sdk.actions.HoverAction;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.requests.Request;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.Utils;

import java.util.List;

public class TransferViewModel extends AbstractFormViewModel {
	final private String TAG = "TransferViewModel";

	private MutableLiveData<String> amount = new MutableLiveData<>();
	private MutableLiveData<StaxContact> contact = new MutableLiveData<>();
	private MutableLiveData<String> note = new MutableLiveData<>();

	protected LiveData<Request> request = new MutableLiveData<>();

	public TransferViewModel(Application application) {
		super(application);
	}

	void setType(String transaction_type) {
		type = transaction_type;
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

	void setContact(String contact_ids) {
		if (contact_ids == null) return;
		new Thread(() -> {
			List<StaxContact> contacts = repo.getContacts(contact_ids.split(","));
			if (contacts.size() > 0) contact.postValue(contacts.get(0));
		}).start();
	}

	void setContact(StaxContact c) {
		contact.setValue(c);
	}

	LiveData<StaxContact> getContact() {
		if (contact == null) { contact = new MutableLiveData<>(); }
		return contact;
	}

	void setRecipient(String r) {
		if (contact.getValue() != null && contact.getValue().toString().equals(r)) { return; }
		contact.setValue(new StaxContact(r));
	}

	public LiveData<Schedule> getSchedule() {
		if (schedule == null) { schedule = new MutableLiveData<>(); }
		return schedule;
	}

	public LiveData<Request> getRequest() {
		if (request == null) { request = new MutableLiveData<>(); }
		return request;
	}

	void setNote(String r) {
		note.postValue(r);
	}

	LiveData<String> getNote() {
		if (note == null) {
			note = new MutableLiveData<>();
			note.setValue(" ");
		}
		return note;
	}

	String amountErrors() {
		if (amount.getValue() != null && !amount.getValue().isEmpty() && amount.getValue().matches("[\\d.]+") && !amount.getValue().matches("[0]+"))
			return null;
		return getApplication().getString(R.string.amount_fielderror);
	}

	String recipientErrors(HoverAction a) {
		if (a != null && a.requiresRecipient() && contact.getValue() == null)
			return getApplication().getString(a.isPhoneBased() ? R.string.transfer_error_recipient_phone : R.string.transfer_error_recipient_account);
		return null;
	}

	public LiveData<Request> decrypt(String encryptedLink) {
		request = repo.decrypt(encryptedLink, getApplication());
		return request;
	}

	public void view(Schedule s) {
		schedule.setValue(s);
		setType(s.type);
		setAmount(s.amount);
		setContact(s.recipient_ids);
		setNote(s.note);
	}

	public void view(Request r) {
		setAmount(r.amount);
		setContact(r.requestee_ids);
		setNote(r.note);
	}

	public void checkSchedule() {
		if (schedule.getValue() != null) {
			Schedule s = schedule.getValue();
			if (s.end_date <= DateUtils.today()) {
				s.complete = true;
				repo.update(s);
			}
		}
	}

	public void saveContact() {
		if (contact.getValue() != null) {
			new Thread(() -> {
				StaxContact c = contact.getValue();
				c.lastUsedTimestamp = DateUtils.now();
				repo.insertOrUpdate(contact.getValue());
			}).start();
		}
	}
}