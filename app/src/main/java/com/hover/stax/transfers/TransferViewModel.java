package com.hover.stax.transfers;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.utils.StagedViewModel;

import java.util.List;

public class TransferViewModel extends StagedViewModel {

	private String type = Action.P2P;
	private LiveData<List<Channel>> selectedChannels = new MutableLiveData<>();
	private MutableLiveData<TransferStage> inputStage = new MutableLiveData<>();

	private MediatorLiveData<Channel> activeChannel = new MediatorLiveData<>();
	private LiveData<List<Action>> filteredActions = new MutableLiveData<>();
	private MediatorLiveData<Action> activeAction = new MediatorLiveData<>();

	private MutableLiveData<String> amount = new MutableLiveData<>();
	private MutableLiveData<String> recipient = new MutableLiveData<>();
	private MutableLiveData<String> note = new MutableLiveData<>();

	public TransferViewModel(Application application) {
		super(application);

		selectedChannels = repo.getSelected();
		activeChannel.addSource(selectedChannels, this::findActiveChannel);
		filteredActions = Transformations.switchMap(activeChannel, this::loadActions);
		activeAction.addSource(filteredActions, this::findActiveAction);

		inputStage.setValue(TransferStage.AMOUNT);
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

	void setRecipient(String r) {
		recipient.postValue(r);
	}

	LiveData<String> getRecipient() {
		if (recipient == null) {
			recipient = new MutableLiveData<>();
		}
		return recipient;
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

	boolean stageValidates() {
		if (stage.getValue() == null) return false;
		switch ((TransferStage) stage.getValue()) {
			case TO_NETWORK:
				return filteredActions.getValue() != null && filteredActions.getValue().size() > 0 && (filteredActions.getValue().size() > 1 || filteredActions.getValue().get(0).hasToInstitution());
			case RECIPIENT:
				return activeAction.getValue() != null && activeAction.getValue().requiresRecipient();
			case REASON:
				return activeAction.getValue() != null && activeAction.getValue().requiresReason();
			default:
				return true;
		}
	}

	void schedule(Context c) {
		Amplitude.getInstance().logEvent(c.getString(R.string.scheduled_transaction, type));
		Schedule s = new Schedule(activeAction.getValue(), futureDate.getValue(), recipient.getValue(), amount.getValue(), note.getValue(), getApplication());
		repo.insert(s);
	}
}