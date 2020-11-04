package com.hover.stax.requests;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.R;
import com.hover.stax.database.Constants;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.StagedViewModel;
import com.hover.stax.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hover.stax.requests.RequestStage.*;

public class NewRequestViewModel extends StagedViewModel {

	private String type = Constants.REQUEST_TYPE;

	private MutableLiveData<String> amount = new MutableLiveData<>();
	private MutableLiveData<List<String>> recipients = new MutableLiveData<>();
	private MutableLiveData<String> note = new MutableLiveData<>();

	private MutableLiveData<Integer> recipientError = new MutableLiveData<>();

	private MutableLiveData<Boolean> requestStarted = new MutableLiveData<>();

	public NewRequestViewModel(@NonNull Application application) {
		super(application);
		stage.setValue(RequestStage.RECIPIENT);
		recipients.setValue(new ArrayList<>(Collections.singletonList("")));
		requestStarted.setValue(false);
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

	public void onUpdate(int pos, String recipient) {
		List<String> rs = recipients.getValue() != null ? recipients.getValue() : new ArrayList<>();
		rs.set(pos, recipient);
		recipients.postValue(rs);
	}

	void addRecipient(String recipient) {
		List<String> rList = recipients.getValue() != null ? recipients.getValue() : new ArrayList<>();
		rList.add(recipient);
		recipients.postValue(rList);
	}

	LiveData<List<String>> getRecipients() {
		if (recipients == null) {
			recipients = new MutableLiveData<>();
		}
		return recipients;
	}

	void resetRecipients() { recipients.setValue(new ArrayList<>()); }

	LiveData<Integer> getRecipientError() {
		if (recipientError == null) {
			recipientError = new MutableLiveData<>();
		}
		return recipientError;
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
		recipients.setValue(new ArrayList<>(Collections.singletonList(s.recipient)));
		setNote(s.note);
		setStage(REVIEW_DIRECT);
	}

	boolean stageValidates() {
		if (stage.getValue() == null) return false;
		switch ((RequestStage) stage.getValue()) {
			case RECIPIENT:
				if (recipients.getValue() == null || recipients.getValue().size() == 0 || recipients.getValue().get(0).isEmpty()) {
					recipientError.setValue(R.string.recipient_fielderror);
					return false;
				} else {
					recipientError.setValue(null);
					List<String> rs = new ArrayList<>();
					for (String r: recipients.getValue()) {
						if (r != null && !r.isEmpty())
							rs.add(r);
					}
					recipients.postValue(rs);
				}
				break;
		}
		return true;
	}

	String generateRecipientString() {
		StringBuilder phones = new StringBuilder();
		List<String> rs = recipients.getValue();
		for (int r = 0; r < rs.size(); r++) {
			phones.append(rs.get(r));
			if (rs.size() > r + 1) phones.append(",");
		}
		return phones.toString();
	}

	String generateSMS(Context c) {
		String a = amount.getValue() != null ? c.getString(R.string.sms_amount_detail, Utils.formatAmount(amount.getValue())) : "";
		String n = note.getValue() != null ? c.getString(R.string.sms_note_detail, note.getValue()) : "";
		return c.getString(R.string.sms_request_template, a, n);
	}

	void saveToDatabase(Context c) {
		for (String recipient : recipients.getValue())
			repo.insert(new Request(recipient, amount.getValue(), note.getValue()));

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
		requestStarted.setValue(true);
	}
	LiveData<Boolean> getStarted() {
		if (requestStarted == null) { requestStarted = new MutableLiveData<>(); }
		return requestStarted;
	}

	public void schedule() {
		Schedule s = new Schedule(futureDate.getValue(), repeatSaved.getValue(), frequency.getValue(), endDate.getValue(),
			generateRecipientString(), amount.getValue(), note.getValue(), getApplication());
		saveSchedule(s);
	}
}
