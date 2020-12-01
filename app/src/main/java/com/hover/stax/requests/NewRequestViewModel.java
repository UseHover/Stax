package com.hover.stax.requests;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

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
import com.hover.stax.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hover.stax.requests.RequestStage.*;

public class NewRequestViewModel extends StagedViewModel {

	private String type = Constants.REQUEST_TYPE;
	private LiveData<List<Channel>> selectedChannels = new MutableLiveData<>();
	private MediatorLiveData<Channel> activeChannel = new MediatorLiveData<>();
	private MutableLiveData<String> amount = new MutableLiveData<>();

	private MutableLiveData<String> requesterNumber = new MutableLiveData<>();
	private MutableLiveData<List<StaxContact>> requestees = new MutableLiveData<>();
	private MutableLiveData<String> note = new MutableLiveData<>();

	private MutableLiveData<Integer> recipientError = new MutableLiveData<>();
	private MutableLiveData<Integer> receivingAccountNumberError = new MutableLiveData<>();
	private MutableLiveData<Integer> receivingAccountChoiceError = new MutableLiveData<>();

	private MutableLiveData<Boolean> requestStarted = new MutableLiveData<>();

	public NewRequestViewModel(@NonNull Application application) {
		super(application);
		selectedChannels = repo.getSelected();
		activeChannel.addSource(selectedChannels, this::findActiveChannel);
		stage.setValue(AMOUNT);
		requestees.setValue(new ArrayList<>(Collections.singletonList(new StaxContact(""))));
		requestStarted.setValue(false);
	}

	private void findActiveChannel(List<Channel> channels) {
		if (channels != null && channels.size() > 0) {
			activeChannel.setValue(channels.get(0));
		}
	}

	void setActiveChannel(int channel_id) {
		if (selectedChannels.getValue() == null || selectedChannels.getValue().size() == 0) {
			return;
		}
		for (Channel c : selectedChannels.getValue()) {
			if (c.id == channel_id)
				activeChannel.setValue(c);
		}
	}
	LiveData<Channel> getActiveChannel() {
		return activeChannel;
	}
	LiveData<List<Channel>> getSelectedChannels() {
		return selectedChannels;
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

	void setRequesterNumber(String number) { requesterNumber.postValue(number); }
	LiveData<String> getRequesterNumber() {
		if(requesterNumber ==null) {
			requesterNumber = new MutableLiveData<>();
		}
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

	LiveData<List<StaxContact>> getRecipients() {
		if (requestees == null) {
			requestees = new MutableLiveData<>();
		}
		return requestees;
	}

	void resetRecipients() { requestees.setValue(new ArrayList<>()); }

	LiveData<Integer> getRecipientError() {
		if (recipientError == null) {
			recipientError = new MutableLiveData<>();
		}
		return recipientError;
	}

	LiveData<Integer> getReceivingAccountChoiceError() {
		if(receivingAccountChoiceError == null) {
			receivingAccountChoiceError = new MutableLiveData<>();
		}
		return receivingAccountChoiceError;
	}
	LiveData<Integer> getReceivingAccountNumberError() {
		if(receivingAccountNumberError == null) {
			receivingAccountNumberError = new MutableLiveData<>();
		}
		return receivingAccountNumberError;
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
		requestees.setValue(repo.getContacts(s.recipient_ids.split(",")));
		setNote(s.note);
		setStage(REVIEW_DIRECT);
	}

	boolean stageValidates() {
		if (stage.getValue() == null) return false;
		switch ((RequestStage) stage.getValue()) {
			case RECIPIENT:
				if (requestees.getValue() == null || requestees.getValue().size() == 0 || requestees.getValue().get(0).phoneNumber.isEmpty()) {
					recipientError.setValue(R.string.recipient_fielderror);
					return false;
				} else {
					recipientError.setValue(null);
					List<StaxContact> cs = new ArrayList<>();
					for (StaxContact r: requestees.getValue()) {
						if (r != null && !r.phoneNumber.isEmpty())
							cs.add(r);
					}
					requestees.postValue(cs);
				}
				break;

			case RECEIVING_ACCOUNT_INFO:
				if(getActiveChannel().getValue() == null) {
					receivingAccountChoiceError.setValue(R.string.receiving_account_choice_error);
					return false;
				}else receivingAccountChoiceError.setValue(null);

				if(requesterNumber.getValue() == null || requesterNumber.getValue().isEmpty()) {
					receivingAccountNumberError.setValue(R.string.receiving_account_number_fielderror);
					return false;
				}else receivingAccountNumberError.setValue(null);
				break;
		}
		return true;
	}

	String generateRecipientString() {
		StringBuilder phones = new StringBuilder();
		List<StaxContact> rs = requestees.getValue();

		for (int r = 0; r < rs.size(); r++) {
			if (phones.length() > 0) phones.append(",");
			phones.append(rs.get(r).phoneNumber);
		}
		return phones.toString();
	}

	String generateSMS() {
		String amountString = amount.getValue() != null ? getApplication().getString(R.string.sms_amount_detail, Utils.formatAmount(amount.getValue())) : "";
		String noteString = note.getValue() != null ? getApplication().getString(R.string.sms_note_detail, note.getValue()) : "";

		String amountNoFormat = amount.getValue() != null ? amount.getValue() : "0.00";
		int institution_id = activeChannel.getValue() !=null ? activeChannel.getValue().institutionId : 0;
		String accountNumber= requesterNumber.getValue() !=null ? requesterNumber.getValue().trim() : "";

		String paymentLink = Request.generateStaxLink(amountNoFormat, institution_id, accountNumber, DateUtils.now(), getApplication());

		if (paymentLink !=null) return getApplication().getString(R.string.sms_request_template, amountString, noteString+" "+paymentLink);
		else return getApplication().getString(R.string.sms_request_template, amountString, noteString);
	}

	void getCountryAlphaAndSendWithWhatsApp(Context context, Activity activity) {
		Channel channel = getActiveChannel().getValue();
		if(channel!=null) {
			saveToDatabase();
			Request.sendUsingWhatsapp(generateRecipientString(), channel.countryAlpha2, generateSMS(), context, activity);
		}
	}

	void saveToDatabase() {
		saveContacts();
		for (StaxContact recipient : requestees.getValue())
			repo.insert(new Request(recipient, amount.getValue(), note.getValue(), getRequesterNumber().getValue(), getActiveChannel().getValue().institutionId, getApplication()));

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
			requestees.getValue(), amount.getValue(), note.getValue(), getApplication());
		saveSchedule(s);
	}

	public void saveContacts() {
		if (requestees.getValue() != null) {
			new Thread(() -> {
				for (StaxContact c: requestees.getValue()) {
					StaxContact existing = repo.getContact(c.id);
					if (existing == null || !existing.equals(c))
						repo.insert(c);
				}
			}).start();
		}
	}
}
