package com.hover.stax.channels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.transactions.StaxTransaction;
import com.hover.stax.transactions.UssdCallResponse;

import java.util.Calendar;
import java.util.List;

public class ChannelsDetailsViewModel extends AndroidViewModel {
	private DatabaseRepo repo;

	private MutableLiveData<Channel> channel;
	private MutableLiveData<List<StaxTransaction>> transactions;
	private LiveData<Double> spentThisMonth;
	private LiveData<Double> spendingDiff;

	public ChannelsDetailsViewModel(@NonNull Application application) {
		super(application);
		channel = new MutableLiveData<>();
		transactions = new MutableLiveData<>();
		repo = new DatabaseRepo(application);

		spentThisMonth = Transformations.switchMap(channel, this::getThisMonth);
		spendingDiff = Transformations.map(spentThisMonth, this::calcDiff);
	}

	void setChannel(int channelId) {
		new Thread(() -> {
			channel.postValue(repo.getChannel(channelId));
			transactions.postValue(repo.getCompleteTransferTransactionsByChannelId(channelId).getValue());
		}).start();
	}

	LiveData<Channel> getChannel() { return channel;}

	LiveData<List<StaxTransaction>> getStaxTransactions() { return transactions; }

	LiveData<Double> getThisMonth(Channel channel) {
		Calendar c = Calendar.getInstance();
		c.get(Calendar.MONTH);
		return repo.getSpentAmount(channel.id, c.get(Calendar.MONTH), c.get(Calendar.YEAR));
	}

	LiveData<Double> getSpentThisMonth() {return spentThisMonth; }

	private Double calcDiff(Double thisMonthAmount) {
		if (thisMonthAmount == null || channel.getValue() == null) return null;
		Calendar c = Calendar.getInstance();
		int month = c.get(Calendar.MONTH) - 1;
		int year = c.get(Calendar.YEAR);
		if (month == 0) {
			month = 12;
			year = year - 1;
		}

		Double lastMonthAmount = repo.getSpentAmount(channel.getValue().id, month, year).getValue();
		return thisMonthAmount - (lastMonthAmount != null ? lastMonthAmount : 0);
	}
	LiveData<Double> getDiff() {return spendingDiff; }
}


