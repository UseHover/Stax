package com.hover.stax.transfers;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.contacts.ContactDao;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.transactions.StaxTransaction;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.StagedViewModel;

import java.util.List;

import static com.hover.stax.transfers.TransferStage.REVIEW;
import static com.hover.stax.transfers.TransferStage.REVIEW_DIRECT;

public class TransferViewModel extends StagedViewModel {
	final private String TAG = "TransferViewModel";

	private String type = Action.P2P;
	private LiveData<List<Channel>> selectedChannels = new MutableLiveData<>();

	private MediatorLiveData<Channel> activeChannel = new MediatorLiveData<>();
	private LiveData<List<Action>> filteredActions = new MutableLiveData<>();
	private MediatorLiveData<Action> activeAction = new MediatorLiveData<>();

	private MutableLiveData<String> amount = new MutableLiveData<>();
	private MutableLiveData<StaxContact> contact = new MutableLiveData<>();
	private MutableLiveData<String> note = new MutableLiveData<>();

	private MutableLiveData<Integer> amountError = new MutableLiveData<>();
	private MutableLiveData<Integer> recipientError = new MutableLiveData<>();

	public TransferViewModel(Application application) {
		super(application);
		stage.setValue(TransferStage.AMOUNT);

		selectedChannels = repo.getSelected();
		activeChannel.addSource(selectedChannels, this::findActiveChannel);
		filteredActions = Transformations.switchMap(activeChannel, this::loadActions);
		activeAction.addSource(filteredActions, this::findActiveAction);
	}

	void setType(String transaction_type) {
		type = transaction_type;
	}

	String getType() {
		return type;
	}

	private void findActiveChannel(List<Channel> channels) {
		if (channels != null && channels.size() > 0) {
			activeChannel.setValue(channels.get(0));
		}
	}

	void setActiveChannel(String channelString) {
		if (selectedChannels.getValue() == null || selectedChannels.getValue().size() == 0) {
			return;
		}
		for (Channel c : selectedChannels.getValue()) {
			if (c.toString().equals(channelString))
				activeChannel.setValue(c);
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

	public LiveData<List<Action>> loadActions(Channel channel) {
		if (channel != null)
			filteredActions = repo.getLiveActions(channel.id, type);
		return filteredActions;
	}

	LiveData<List<Channel>> getSelectedChannels() {
		return selectedChannels;
	}

	LiveData<List<Action>> getActions() {
		return filteredActions;
	}

	private void findActiveAction(List<Action> actions) {
		if (actions != null && actions.size() > 0 && activeAction.getValue() == null) {
			activeAction.setValue(actions.get(0));
		}
	}

	void setActiveAction(Action action) {
		activeAction.postValue(action);
	}
	void setActiveAction(String actionString) {
		if (filteredActions.getValue() == null || filteredActions.getValue().size() == 0) {
			return;
		}
		for (Action a : filteredActions.getValue()) {
			if (a.toString().equals(actionString))
				activeAction.postValue(a);
		}
	}

	LiveData<Action> getActiveAction() {
		if (activeAction == null) {
			activeAction = new MediatorLiveData<>();
		}
		return activeAction;
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
		if (amountError == null) { amountError = new MutableLiveData<>(); }
		return amountError;
	}

	void setContact(String contact_ids) {
		new Thread(() -> contact.postValue(repo.getContacts(contact_ids.split(",")).get(0))).start();
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
		if (recipientError == null) { recipientError = new MutableLiveData<>(); }
		return recipientError;
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

	@Override
	public void goToNextStage() {
		stage.postValue(goToNextStage(stage.getValue()));
	}

	private StagedEnum goToNextStage(StagedEnum currentStage) {
		StagedEnum next = currentStage.next();
		if (!stageRequired((TransferStage) next))
			next = goToNextStage(next);
		return next;
	}

	boolean stageRequired(TransferStage ts) {
		switch (ts) {
			case TO_NETWORK:
				return requiresActionChoice();
			case RECIPIENT:
				return activeAction.getValue() != null && activeAction.getValue().requiresRecipient();
			case NOTE:
				return activeAction.getValue() != null && activeAction.getValue().requiresReason();
			default:
				return true;
		}
	}

	boolean requiresActionChoice() { // in last case, should have request network as choice
		return filteredActions.getValue() != null && filteredActions.getValue().size() > 0 && (filteredActions.getValue().size() > 1 || filteredActions.getValue().get(0).hasDiffToInstitution());
	}

	boolean hasActionsLoaded() {
		return filteredActions.getValue() != null && filteredActions.getValue().size() > 0 &&
			(filteredActions.getValue().size() > 1 || (activeAction.getValue() != null && activeAction.getValue().hasToInstitution()));
	}

	boolean stageValidates() {
		if (stage.getValue() == null) return false;
		switch ((TransferStage) stage.getValue()) {
			case AMOUNT:
				if (amount.getValue() == null || amount.getValue().isEmpty()) {
					amountError.setValue(R.string.amount_fielderror);
					return false;
				} else
					amountError.setValue(null);
				break;
			case RECIPIENT:
				if (contact.getValue() == null || contact.getValue().phoneNumber.isEmpty()) {
					recipientError.setValue(R.string.recipient_fielderror);
					return false;
				} else
					recipientError.setValue(null);
				break;
		}
		return true;
	}

	boolean isDone() { return stage.getValue() == REVIEW || stage.getValue() == REVIEW_DIRECT; }

	public void view(Schedule s) {
		schedule.setValue(s);
		setType(s.type);
		setActiveChannel(s.channel_id);
		setAmount(s.amount);
		setContact(s.recipient_ids);
		setNote(s.note);
		setStage(TransferStage.REVIEW_DIRECT);
	}

	public void schedule() {
		Schedule s = new Schedule(activeAction.getValue(), futureDate.getValue(), repeatSaved.getValue(), frequency.getValue(), endDate.getValue(),
			contact.getValue(), amount.getValue(), note.getValue(), getApplication());
		saveSchedule(s);
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
				StaxContact c = repo.getContact(contact.getValue().id);
				if (c == null || !c.equals(contact.getValue()))
					repo.insert(contact.getValue());
			}).start();
		}
	}
}