package com.hover.stax.transfers;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.utils.fieldstates.FieldState;
import com.hover.stax.utils.fieldstates.FieldStateType;
import com.hover.stax.utils.fieldstates.Validation;
import com.hover.stax.requests.Request;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.utils.DateUtils;

import java.util.List;

public class TransferViewModel extends AbstractFormViewModel {
	final private String TAG = "TransferViewModel";

	private MutableLiveData<String> amount = new MutableLiveData<>();
	private MutableLiveData<StaxContact> contact = new MutableLiveData<>();
	private MutableLiveData<String> note = new MutableLiveData<>();

	private MediatorLiveData<FieldState> amountFieldState = new MediatorLiveData<>();
	private MediatorLiveData<FieldState> recipientFieldState = new MediatorLiveData<>();

	protected LiveData<Request> request = new MutableLiveData<>();

	public TransferViewModel(Application application) {
		super(application);
		amountFieldState.addSource(amount, amount -> { if (amount != null) amountFieldState.setValue(null); });
		amountFieldState.addSource(contact, contact -> { if (contact != null) amountFieldState.setValue(null); });
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

	LiveData<FieldState> getAmountFieldState() {
		if (amountFieldState == null) { amountFieldState = new MediatorLiveData<>(); }
		return amountFieldState;
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
		recipientFieldState.postValue(new FieldState(FieldStateType.SUCCESS, ""));
	}

	LiveData<StaxContact> getContact() {
		if (contact == null) { contact = new MutableLiveData<>(); }
		return contact;
	}

	void setRecipient(String r) {
		if (contact.getValue() != null && contact.getValue().toString().equals(r)) { return; }
		contact.setValue(new StaxContact(r));
	}

	LiveData<FieldState> getRecipientFieldState() {
		if (recipientFieldState == null) { recipientFieldState = new MediatorLiveData<>(); }
		return recipientFieldState;
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

	protected boolean validates(Action a, Validation validationType) {
		boolean valid = true;
		if (amount.getValue() == null || amount.getValue().isEmpty() || !amount.getValue().matches("[\\d.]+") || Double.parseDouble(amount.getValue()) < 1) {
			if(validationType == Validation.HARD) {
				valid = false;
				amountFieldState.setValue(new FieldState(FieldStateType.ERROR,getApplication().getString(R.string.amount_fielderror)));
			}
		} else amountFieldState.setValue(new FieldState(FieldStateType.SUCCESS, ""));

		if(a!=null) {
			if (a.requiresRecipient() && contact.getValue() == null) {
				if(validationType == Validation.HARD) {
					valid = false;
					recipientFieldState.setValue(new FieldState(FieldStateType.ERROR, getApplication().getString(R.string.transfer_error_recipient)));
				}
			} else recipientFieldState.setValue(new FieldState(FieldStateType.SUCCESS, ""));
		}else valid = false;

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