package com.hover.stax.pins;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;

import java.util.ArrayList;
import java.util.List;

public class PinsViewModel extends AndroidViewModel {

	private DatabaseRepo repo;

	private LiveData<List<Channel>> channels;

	public PinsViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		loadSelectedChannels();
	}

	public LiveData<List<Channel>> getSelectedChannels() { return channels; }

	private void loadSelectedChannels() {
		if (channels == null) {
			channels = new MutableLiveData<>();
		}
		channels = repo.getSelected();
	}

	public void savePins() {
		List<Channel> selectedChannels = channels.getValue() != null ? channels.getValue() : new ArrayList<>();
		for (Channel channel: selectedChannels) {
			if (channel.pin != null) {
				repo.update(channel);
			}
		}
	}

	public void setDefaultAccount(int channelId) {

	}
}
