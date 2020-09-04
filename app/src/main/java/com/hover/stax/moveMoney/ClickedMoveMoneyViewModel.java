package com.hover.stax.moveMoney;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hover.stax.channels.Channel;

public class ClickedMoveMoneyViewModel extends ViewModel {
	private MutableLiveData<Channel> fromWhichChannel;
	private MutableLiveData<Channel> toWhichChannel;

	public ClickedMoveMoneyViewModel() {
		fromWhichChannel = new MutableLiveData<>();
		toWhichChannel = new MutableLiveData<>();
	}

	void getToWhichChannels(String fromChannelId) {

	}


}
