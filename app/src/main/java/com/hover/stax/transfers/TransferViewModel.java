package com.hover.stax.transfers;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
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

	private MutableLiveData<Channel> activeChannel = new MutableLiveData<>();
	private Action activeAction;
	private MutableLiveData<InputStage> inputStageMutableLiveData = new MutableLiveData<>();

	private MutableLiveData<Boolean> futureDated = new MutableLiveData<>();
	private MutableLiveData<Long> futureDate = new MutableLiveData<>();

	private DatabaseRepo repo;

	public TransferViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		loadSelected();
		loadDefault();
		filteredActions = Transformations.switchMap(activeChannel, this::loadActions);
		inputStageMutableLiveData.setValue(InputStage.AMOUNT);
		futureDated.setValue(false);
		futureDate.setValue(null);
	}

	void setInputStage(InputStage stage) {inputStageMutableLiveData.postValue(stage);}
	LiveData<InputStage> stageLiveData() {return inputStageMutableLiveData;}

	void setType(String transaction_type) {
		type = transaction_type;
	}

	private void loadSelected() {
		if (selectedChannels == null) {
			selectedChannels = new MutableLiveData<>();
		}
		selectedChannels = repo.getSelected();
	}

	private void loadDefault() {
		if (activeChannel == null) {
			activeChannel = new MutableLiveData<>();
		}
		activeChannel.setValue(repo.getDefault().getValue());
	}

	void setActiveChannel(Channel c) {
		activeChannel.setValue(c);
	}

	LiveData<Channel> getActiveChannel() {
		return activeChannel;
	}

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

	void setActiveAction(Action action) {
		activeAction = action;
	}

	Action getActiveAction() {
		return activeAction;
	}

	void setIsFutureDated(boolean isFuture) { futureDated.setValue(isFuture); }
	LiveData<Boolean> getIsFuture() {
		return futureDated;
	}
	void setFutureDate(Long date) { futureDate.setValue(date); }
	LiveData<Long> getFutureDate() {
		return futureDate;
	}

	void schedule(String recipient, String amount) {
		Schedule s = new Schedule(activeAction, futureDate.getValue(), recipient, amount, getApplication());
		repo.insert(s);
	}
}