package com.hover.stax.requests;

import android.app.Application;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.textfield.TextInputEditText;
import com.hover.stax.R;
import com.hover.stax.utils.StagedViewModel;
import com.hover.stax.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.view.View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION;
import static com.hover.stax.requests.RequestStage.AMOUNT;
import static com.hover.stax.requests.RequestStage.NOTE;
import static com.hover.stax.requests.RequestStage.RECIPIENT;
import static com.hover.stax.requests.RequestStage.REVIEW;
import static com.hover.stax.requests.RequestStage.REVIEW_DIRECT;

public class NewRequestViewModel extends StagedViewModel {

	private MutableLiveData<String> amount = new MutableLiveData<>();
	private MutableLiveData<List<String>> recipients = new MutableLiveData<>();
	private MutableLiveData<String> note = new MutableLiveData<>();

	private MutableLiveData<Integer> recipientError = new MutableLiveData<>();

	public NewRequestViewModel(@NonNull Application application) {
		super(application);
		stage.setValue(RequestStage.RECIPIENT);
		recipients.setValue(new ArrayList<>(Collections.singletonList("")));
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

	boolean stageValidates() {
		if (stage.getValue() == null) return false;
		switch ((RequestStage) stage.getValue()) {
			case RECIPIENT:
				if (recipients.getValue() == null || recipients.getValue().size() == 0 || recipients.getValue().get(0).isEmpty()) {
					recipientError.setValue(R.string.enterRecipientError);
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
		String a = amount.getValue() != null ? c.getString(R.string.amount_detail, Utils.formatAmount(amount.getValue())) : "";
		String n = note.getValue() != null ? c.getString(R.string.note_detail, note.getValue()) : "";
		return c.getString(R.string.request_money_sms_template, a, n);
	}

	void saveToDatabase() {
		for (String recipient : recipients.getValue())
			repo.insert(new Request(recipient, amount.getValue(), note.getValue()));
	}
}
