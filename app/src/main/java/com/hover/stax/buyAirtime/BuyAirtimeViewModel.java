package com.hover.stax.buyAirtime;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;

import java.util.List;

public class BuyAirtimeViewModel extends AndroidViewModel {

	private LiveData<List<Channel>> selectedChannels;
	private LiveData<List<Action>> airtimeActions;

	private MutableLiveData<Channel> activeChannel = new MutableLiveData<>();
	private Action activeAction;

	private DatabaseRepo repo;

	public BuyAirtimeViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		loadSelected();
		loadDefault();
		airtimeActions = Transformations.switchMap(getActiveChannel(), this::loadActions);
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

	void setActiveChannel(Channel c) { activeChannel.setValue(c); }

	LiveData<Channel> getActiveChannel() { return activeChannel; }

	public LiveData<List<Action>> loadActions(Channel channel) {
		if (channel != null)
			return repo.getActions(channel.id, "airtime");
		else return null;
	}

	LiveData<List<Channel>> getSelectedChannels() {
		return selectedChannels;
	}

	LiveData<List<Action>> getActions() { return airtimeActions; }

	void setActiveAction(Action action) { activeAction = action; }

	Action getActiveAction() { return activeAction; }
}