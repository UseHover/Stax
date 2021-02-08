package com.hover.stax.transfers;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.requests.Request;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.AbstractFormViewModel;

import java.util.List;

public class TransferViewModel extends AbstractFormViewModel {
	final private String TAG = "TransferViewModel";

	private MutableLiveData<String> amount = new MutableLiveData<>();
	private MutableLiveData<StaxContact> contact = new MutableLiveData<>();
	private MutableLiveData<String> note = new MutableLiveData<>();

	private MediatorLiveData<Integer> amountError = new MediatorLiveData<>();
	private MediatorLiveData<Integer> recipientError = new MediatorLiveData<>();

	protected LiveData<Request> request = new MutableLiveData<>();

	public TransferViewModel(Application application) {
		super(application);
		amountError.addSource(amount, amount -> { if (amount != null) amountError.setValue(null); });
		recipientError.addSource(contact, contact -> { if (contact != null) recipientError.setValue(null); });
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

	LiveData<Integer> getAmountError() {
		if (amountError == null) { amountError = new MediatorLiveData<>(); }
		return amountError;
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

	LiveData<Integer> getRecipientError() {
		if (recipientError == null) { recipientError = new MediatorLiveData<>(); }
		return recipientError;
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

	protected boolean validates(Action a) {
		boolean valid = true;
		if (amount.getValue() == null || amount.getValue().isEmpty() || Double.parseDouble(amount.getValue()) < 1) {
			valid = false;
			amountError.setValue(R.string.amount_fielderror);
		}
		if (a.requiresRecipient() && contact.getValue() == null) {
			valid = false;
			recipientError.setValue(R.string.transfer_error_recipient);
		}
		Log.e(TAG, "is valid? " + valid);
		return valid;
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