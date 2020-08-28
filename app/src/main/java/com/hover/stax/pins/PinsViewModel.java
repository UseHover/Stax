package com.hover.stax.pins;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.ApplicationInstance;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.database.KeyStoreExecutor;

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

	void savePins(List<Channel> updatedChannels, Context c) {

		for (Channel channel: updatedChannels) {
			if (channel.pin != null) {
				channel.pin = KeyStoreExecutor.createNewKey(channel.pin, c);

				repo.update(channel);
			}
		}
	}

	public void setDefaultAccount(Channel channel) {
		repo.update(channel);
	}
}
