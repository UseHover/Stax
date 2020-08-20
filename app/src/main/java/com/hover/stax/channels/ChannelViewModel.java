package com.hover.stax.channels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.sdk.api.Hover;
import com.hover.sdk.sims.SimInfo;
import com.hover.stax.database.DatabaseRepo;

import java.util.ArrayList;
import java.util.List;

public class ChannelViewModel extends AndroidViewModel {

	private DatabaseRepo repo;

	private MutableLiveData<List<SimInfo>> sims;

	private LiveData<List<Channel>> channels;
	private MutableLiveData<List<Integer>> selected = new MutableLiveData<>();

	public ChannelViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		loadChannels();
		loadSims(application);
	}

	LiveData<List<Channel>> getChannels() { return channels; }

	public MutableLiveData<List<SimInfo>> getSims() { return sims; }

	private void loadChannels() {
		if (channels == null) {
			channels = new MutableLiveData<>();
		}
		channels = repo.getAll();
	}
	private void loadSims(Application application) {
		if (sims == null) {
			sims = new MutableLiveData<>();
		}
		sims.setValue(Hover.getPresentSims(application));
	}

	public LiveData<List<Integer>> getSelected() {
		if (selected == null) {
			selected = new MutableLiveData<>();
		}
		return selected;
	}

	public void setSelected(int id) {
		List<Integer> list = selected.getValue() != null ? selected.getValue() : new ArrayList<>();
		if (list.contains(id))
			list.remove((Integer) id);
		else
			list.add(id);
		selected.setValue(list);
	}
}
