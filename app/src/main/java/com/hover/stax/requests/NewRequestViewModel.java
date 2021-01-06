package com.hover.stax.requests;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.database.Constants;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.StagedViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hover.stax.requests.RequestStage.*;

public class NewRequestViewModel extends StagedViewModel {
	public final static String TAG = "NewRequestViewModel";

	private String type = Constants.REQUEST_TYPE;
	private MutableLiveData<String> amount = new MutableLiveData<>();

	private MutableLiveData<List<StaxContact>> requestees = new MutableLiveData<>();
	private MediatorLiveData<String> requesterNumber = new MediatorLiveData<>();
	private MutableLiveData<String> note = new MutableLiveData<>();

	private MutableLiveData<Integer> requesteeError = new MutableLiveData<>();
	private MutableLiveData<Integer> requesterNumberError = new MutableLiveData<>();
	private MutableLiveData<Integer> requesterAccountError = new MutableLiveData<>();

	private MutableLiveData<Request> formulatedRequest = new MutableLiveData<>();

	public NewRequestViewModel(@NonNull Application application) {
		super(application);
		stage.setValue(AMOUNT);
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

	LiveData<Integer> getRequesteeError() {
		if (requesteeError == null) {
			requesteeError = new MutableLiveData<>();
		}
		return requesteeError;
	}

	LiveData<Integer> getRequesterAccountError() {
		if(requesterAccountError == null) {
			requesterAccountError = new MutableLiveData<>();
		}
		return requesterAccountError;
	}
	LiveData<Integer> getRequesterNumberError() {
		if(requesterNumberError == null) {
			requesterNumberError = new MutableLiveData<>();
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

	boolean isDone() { return stage.getValue() == REVIEW || stage.getValue() == REVIEW_DIRECT; }

	public void setSchedule(Schedule s) {
		schedule.setValue(s);
		setAmount(s.amount);
		new Thread(() -> requestees.setValue(repo.getContacts(s.recipient_ids.split(",")))).start();
		setNote(s.note);
		setStage(REVIEW_DIRECT);
	}

	boolean stageValidates() {
		if (stage.getValue() == null) return false;
		switch ((RequestStage) stage.getValue()) {
			case REQUESTEE:
				if (requestees.getValue() == null || requestees.getValue().size() == 0 || requestees.getValue().get(0).getPhoneNumber().isEmpty()) {
					requesteeError.setValue(R.string.recipient_fielderror);
					return false;
				} else {
					requesteeError.setValue(null);
					List<StaxContact> cs = new ArrayList<>();
					for (StaxContact r: requestees.getValue()) {
						if (r != null && !r.getPhoneNumber().isEmpty())
							cs.add(r);
					}
					requestees.postValue(cs);
				}
				break;
			case REQUESTER:
				if (getActiveChannel().getValue() == null) {
					requesterAccountError.setValue(R.string.requester_account_error);
					return false;
				} else requesterAccountError.setValue(null);

				if (requesterNumber.getValue() == null || requesterNumber.getValue().isEmpty()) {
					requesterNumberError.setValue(R.string.requester_number_fielderror);
					return false;
				} else requesterNumberError.setValue(null);
				break;
		}
		return true;
	}

	LiveData<Request> getRequest() {
		if (formulatedRequest == null) { formulatedRequest = new MutableLiveData<>(); }
		return formulatedRequest;
	}

	void saveToDatabase() {
		repo.update(activeChannel.getValue());

		saveContacts();
		for (StaxContact recipient : requestees.getValue())
			repo.insert(new Request(formulatedRequest.getValue(), recipient, getApplication()));

		if (repeatSaved.getValue() != null && repeatSaved.getValue()) {
			schedule();
		} else if (schedule.getValue() != null) {
			Schedule s = schedule.getValue();
			if (s.end_date <= DateUtils.today()) {
				s.complete = true;
				repo.update(s);
			}
		}
	}

	void setStarted() {
		formulatedRequest.setValue(new Request(amount.getValue(), note.getValue(), getRequesterNumber().getValue(), getActiveChannel().getValue().institutionId));
	}
	Boolean getStarted() {
		return formulatedRequest == null || formulatedRequest.getValue() != null;
	}

	public void schedule() {
		Schedule s = new Schedule(futureDate.getValue(), repeatSaved.getValue(), frequency.getValue(), endDate.getValue(),
			requestees.getValue(), amount.getValue(), note.getValue(), getApplication());
		saveSchedule(s);
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
