package com.hover.stax.transfers;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.schedules.Schedule;

import java.util.List;

public class TransferViewModel extends AndroidViewModel {

	private String type = Action.P2P;
	private LiveData<List<Channel>> selectedChannels;
	private LiveData<List<Action>> filteredActions;

	private MediatorLiveData<Channel> activeChannel = new MediatorLiveData<>();
	private MutableLiveData<Action> activeAction = new MutableLiveData<>();

	private MutableLiveData<InputStage> inputStage = new MutableLiveData<>();
	private MutableLiveData<String> amount = new MutableLiveData<>();
	private MutableLiveData<String> recipient = new MutableLiveData<>();
	private MutableLiveData<String> reason = new MutableLiveData<>();

	private MutableLiveData<Boolean> futureDated = new MutableLiveData<>();
	private MutableLiveData<Long> futureDate = new MutableLiveData<>();

	private DatabaseRepo repo;

	public TransferViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		loadSelected();
		loadDefault();
		filteredActions = Transformations.switchMap(activeChannel, this::loadActions);
		inputStage.setValue(InputStage.AMOUNT);
		futureDated.setValue(false);
		futureDate.setValue(null);
	}

	void goToNextStage() {
		InputStage next = inputStage.getValue() != null ? inputStage.getValue().next() : InputStage.AMOUNT;
		if (next.compareTo(InputStage.REASON) == 0 && !activeAction.getValue().requiresReason())
			next = next.next();
		inputStage.postValue(next);
	}
	void goToPrevStage() { inputStage.postValue(inputStage.getValue() != null ? inputStage.getValue().prev() : InputStage.AMOUNT);}
	LiveData<InputStage> getStage() {
		if (inputStage == null) { inputStage = new MutableLiveData<>(); inputStage.setValue(InputStage.AMOUNT);}
		return inputStage;
	}

	void setAmount(String a) { amount.postValue(a); }
	LiveData<String> getAmount() {
		if (amount == null) { amount = new MutableLiveData<>(); }
		return amount;
	}

	void setRecipient(String r) { recipient.postValue(r); }
	LiveData<String> getRecipient() {
		if (recipient == null) { recipient = new MutableLiveData<>(); }
		return recipient;
	}

	void setReason(String r) { reason.postValue(r); }
	LiveData<String> getReason() {
		if (reason == null) { reason = new MutableLiveData<>(); reason.setValue(" "); }
		return reason;
	}

	void setType(String transaction_type) { type = transaction_type; }
	String getType() { return type; }

	private void loadSelected() {
		if (selectedChannels == null) {
			selectedChannels = new MutableLiveData<>();
		}
		selectedChannels = repo.getSelected();
	}

	private void loadDefault() {
		if (activeChannel == null) { activeChannel = new MediatorLiveData<>(); }
		activeChannel.addSource(repo.getDefault(), c -> activeChannel.setValue(c));
	}

	void setActiveChannel(int channel_id) {
		if (selectedChannels.getValue() == null || selectedChannels.getValue().size() == 0) { return; }
		for (Channel c: selectedChannels.getValue()) {
			if (c.id == channel_id)
				activeChannel.setValue(c);
		}
	}

	LiveData<Channel> getActiveChannel() { return activeChannel; }

	public LiveData<List<Action>> loadActions(Channel channel) {
		if (channel != null)
			return repo.getLiveActions(channel.id, type);
		else return new MutableLiveData<>();
	}

	LiveData<List<Channel>> getSelectedChannels() {
		return selectedChannels;
	}

	LiveData<List<Action>> getActions() {
		return filteredActions;
	}

	void setActiveAction(Action action) { activeAction.setValue(action); }
	LiveData<Action> getActiveAction() {
		if (activeAction == null) { activeAction = new MutableLiveData<>(); }
		return activeAction;
	}

	void setIsFutureDated(boolean isFuture) { futureDated.setValue(isFuture); }
	LiveData<Boolean> getIsFuture() {
		if (futureDated == null) { futureDated = new MutableLiveData<>(); futureDated.setValue(false);}
		return futureDated;
	}
	void setFutureDate(Long date) { futureDate.setValue(date); }
	LiveData<Long> getFutureDate() {
		if (futureDate == null) { futureDate = new MutableLiveData<>(); }
		return futureDate;
	}

	void schedule() {
		Schedule s = new Schedule(activeAction.getValue(), futureDate.getValue(), recipient.getValue(), amount.getValue(), getApplication());
		repo.insert(s);
	}
}