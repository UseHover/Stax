package com.hover.stax.transfers;

import android.app.Application;
import android.content.Context;
import android.renderscript.ScriptGroup;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.schedules.Schedule;

import java.util.List;

public class TransferViewModel extends AndroidViewModel {

	private String type = Action.P2P;
	private LiveData<List<Channel>> selectedChannels;
	private MutableLiveData<InputStage> inputStage = new MutableLiveData<>();

	private MediatorLiveData<Channel> activeChannel = new MediatorLiveData<>();
	private LiveData<List<Action>> filteredActions;
	private MutableLiveData<Action> activeAction = new MutableLiveData<>();

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

	void setType(String transaction_type) { type = transaction_type; }
	String getType() { return type; }

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

	void setStage(InputStage stage) { inputStage.setValue(stage); }
	void goToNextStage() {
		InputStage next = inputStage.getValue() != null ? inputStage.getValue().next() : InputStage.AMOUNT;
		next = validateNext(next);
		inputStage.postValue(next);
	}

	private InputStage validateNext(InputStage next) {
		if (!canStayAt(next)) { next = validateNext(next.next()); }
		return next;
	}

	boolean goToPrevStage() {
		if (inputStage.getValue() != null || inputStage.getValue().compareTo(InputStage.AMOUNT) == 0) return false;
		boolean canGoBack = canStayAt(inputStage.getValue().prev());
		if (canGoBack) { inputStage.postValue(inputStage.getValue().prev()); }
		return canGoBack;
	}

	private boolean canStayAt(InputStage stage) {
		switch (stage) {
			case TO_NETWORK: return filteredActions.getValue() != null && (filteredActions.getValue().size() > 1 || filteredActions.getValue().get(0).hasToInstitution());
			case TO_NUMBER: return activeAction.getValue() != null && activeAction.getValue().requiresRecipient();
			case REASON: return activeAction.getValue() != null && activeAction.getValue().requiresReason();
			default: return true;
		}
	}

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

	void schedule(Context c) {
		Amplitude.getInstance().logEvent(c.getString(R.string.scheduled_transaction, type));
		Log.e("VM", "Scheduling, active action: " + activeAction.getValue().public_id);
		Schedule s = new Schedule(activeAction.getValue(), futureDate.getValue(), recipient.getValue(), amount.getValue(), getApplication());
		repo.insert(s);
	}
}