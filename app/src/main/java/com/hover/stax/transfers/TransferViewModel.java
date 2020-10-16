package com.hover.stax.transfers;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.utils.StagedViewModel;

import java.util.List;

import static com.hover.stax.transfers.TransferStage.REVIEW;
import static com.hover.stax.transfers.TransferStage.REVIEW_DIRECT;

public class TransferViewModel extends StagedViewModel {

	private String type = Action.P2P;
	private LiveData<List<Channel>> selectedChannels = new MutableLiveData<>();

	private MediatorLiveData<Channel> activeChannel = new MediatorLiveData<>();
	private LiveData<List<Action>> filteredActions = new MutableLiveData<>();
	private MediatorLiveData<Action> activeAction = new MediatorLiveData<>();

	private MutableLiveData<String> amount = new MutableLiveData<>();
	private MutableLiveData<String> recipient = new MutableLiveData<>();
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
		if (actions != null && actions.size() > 0) {
			activeAction.setValue(actions.get(0));
		}
	}

	void setActiveAction(Action action) {
		activeAction.setValue(action);
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

	void setRecipient(String r) {
		recipient.postValue(r);
	}

	LiveData<String> getRecipient() {
		if (recipient == null) {
			recipient = new MutableLiveData<>();
		}
		return recipient;
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

	private boolean requiresActionChoice() {
		return filteredActions.getValue() != null && filteredActions.getValue().size() > 0 && (filteredActions.getValue().size() > 1 || filteredActions.getValue().get(0).hasToInstitution());
	}

	boolean stageValidates() {
		if (stage.getValue() == null) return false;
		switch ((TransferStage) stage.getValue()) {
			case AMOUNT:
				if (amount.getValue() == null || amount.getValue().isEmpty()) {
					amountError.setValue(R.string.enterAmountError);
					return false;
				} else
					amountError.setValue(null);
				break;
			case RECIPIENT:
				if (recipient.getValue() == null || recipient.getValue().isEmpty()) {
					recipientError.setValue(R.string.enterRecipientError);
					return false;
				} else
					recipientError.setValue(null);
				break;
		}
		return true;
	}

	boolean isDone() { return stage.getValue() == REVIEW || stage.getValue() == REVIEW_DIRECT; }

	public void setSchedule(Schedule s) {
		schedule.setValue(s);
		setType(s.type);
		setActiveChannel(s.channel_id);
		setAmount(s.amount);
		setRecipient(s.recipient);
		setNote(s.note);
		setStage(TransferStage.REVIEW_DIRECT);
	}

	public void schedule() {
		Schedule s = new Schedule(activeAction.getValue(), futureDate.getValue(), repeatSaved.getValue(), frequency.getValue(), endDate.getValue(),
			recipient.getValue(), amount.getValue(), note.getValue(), getApplication());
		saveSchedule(s);
	}
}