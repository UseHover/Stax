package com.hover.stax.requests;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.Constants;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.StagedViewModel;
import com.hover.stax.utils.Utils;
import com.hover.stax.utils.paymentLinkCryptography.Base64;
import com.hover.stax.utils.paymentLinkCryptography.Encryption;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.sentry.Sentry;

import static com.hover.stax.requests.RequestStage.*;

public class NewRequestViewModel extends StagedViewModel {

	private String type = Constants.REQUEST_TYPE;
	private LiveData<List<Channel>> selectedChannels = new MutableLiveData<>();
	private MediatorLiveData<Channel> activeChannel = new MediatorLiveData<>();
	private MutableLiveData<String> amount = new MutableLiveData<>();
	private MutableLiveData<String> receivingAccountNumber = new MutableLiveData<>();
	private MutableLiveData<List<String>> recipients = new MutableLiveData<>();
	private MutableLiveData<String> note = new MutableLiveData<>();

	private MutableLiveData<Integer> recipientError = new MutableLiveData<>();
	private MutableLiveData<Integer> receivingAccountNumberError = new MutableLiveData<>();
	private MutableLiveData<Integer> receivingAccountChoiceError = new MutableLiveData<>();

	private MutableLiveData<Boolean> requestStarted = new MutableLiveData<>();

	public NewRequestViewModel(@NonNull Application application) {
		super(application);
		selectedChannels = repo.getSelected();
		activeChannel.addSource(selectedChannels, this::findActiveChannel);
		stage.setValue(RequestStage.RECIPIENT);
		recipients.setValue(new ArrayList<>(Collections.singletonList("")));
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

	void setReceivingAccountNumber(String number) { receivingAccountNumber.postValue(number); }
	LiveData<String> getReceivingAccountNumber() {
		if(receivingAccountNumber ==null) {
			receivingAccountNumber = new MutableLiveData<>();
		}
		return receivingAccountNumber;
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

			case RECEIVING_ACCOUNT_INFO:
				if(getActiveChannel().getValue() == null) {
					receivingAccountChoiceError.setValue(R.string.receiving_account_choice_error);
					return false;
				}else receivingAccountChoiceError.setValue(null);

				if(receivingAccountNumber.getValue() == null || receivingAccountNumber.getValue().length()<5) {
					receivingAccountNumberError.setValue(R.string.receiving_account_number_fielderror);
					return false;
				}else receivingAccountNumberError.setValue(null);
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
		String amountString = amount.getValue() != null ? c.getString(R.string.sms_amount_detail, Utils.formatAmount(amount.getValue())) : "";
		String noteString = note.getValue() != null ? c.getString(R.string.sms_note_detail, note.getValue()) : "";

		String amountNoFormat = amount.getValue() != null ? amount.getValue() : "0.00";
		int channel_id = activeChannel.getValue() !=null ? activeChannel.getValue().id : 0;
		String accountNumber= receivingAccountNumber.getValue() !=null ? receivingAccountNumber.getValue().trim() : "";

		String paymentLink = generateStaxLink(amountNoFormat, channel_id, accountNumber, c );

		if(paymentLink !=null) return c.getString(R.string.sms_request_template_with_link, amountString, noteString, paymentLink);
		else return c.getString(R.string.sms_request_template_no_link, amountString, noteString);
	}

	private String generateStaxLink(String amount, int channel_id, String accountNumber, Context c) {
	if(channel_id == 0 || accountNumber.isEmpty()) {
		Amplitude.getInstance().logEvent(c.getString(R.string.stax_link_encryption_failure_1));
		return null;
	}
	String separator = "-";
	String fullString = amount+separator+channel_id +separator+accountNumber+separator+DateUtils.today();

		try {
			Encryption encryption =  repo.getEncryptionSettings().build();
			String encryptedString = encryption.encryptOrNull(fullString);
			return Constants.STAX_LINK_PREFIX+encryptedString;

		} catch (NoSuchAlgorithmException e) {
			Amplitude.getInstance().logEvent(c.getString(R.string.stax_link_encryption_failure_2));
			return null;
		}
	}


	void saveToDatabase(Context c) {
		for (String recipient : recipients.getValue())
			repo.insert(new Request(recipient, amount.getValue(), note.getValue(), getActiveChannel().getValue().id, getReceivingAccountNumber().getValue()));

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
