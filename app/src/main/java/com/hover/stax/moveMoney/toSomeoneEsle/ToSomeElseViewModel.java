package com.hover.stax.moveMoney.toSomeoneEsle;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;

import java.util.List;

public class ToSomeElseViewModel extends AndroidViewModel {
	private LiveData<List<Channel>> fromWhichChannel;
	private MutableLiveData<Channel> toWhichChannel;
	private DatabaseRepo repo;

	public ToSomeElseViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		fromWhichChannel = new MutableLiveData<>();
		toWhichChannel = new MutableLiveData<>();
		fromWhichChannel = repo.getSelected();
	}

	LiveData<List<Channel>> getAllSelectedChannels() {return  fromWhichChannel;}
	void getToWhichChannels(String fromChannelId) {

	}


}
