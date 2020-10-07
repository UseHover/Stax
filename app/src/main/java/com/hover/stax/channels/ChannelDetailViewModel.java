package com.hover.stax.channels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.transactions.StaxTransaction;

import java.util.Calendar;
import java.util.List;

public class ChannelDetailViewModel extends AndroidViewModel {
	private static String TAG = "ChannelDetailViewModel";
	private DatabaseRepo repo;

	private MutableLiveData<Integer> id;
	private LiveData<Channel> channel;
	private LiveData<List<StaxTransaction>> transactions;
	private LiveData<Double> spentThisMonth;
	private LiveData<Double> feesThisYear;

	public ChannelDetailViewModel(@NonNull Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		id = new MutableLiveData<>();
		channel = Transformations.switchMap(id, id -> repo.getLiveChannel(id));
		transactions =  Transformations.switchMap(id, id -> repo.getCompleteTransferTransactions(id));
		spentThisMonth = Transformations.switchMap(id, this::loadSpentThisMonth);
		feesThisYear = Transformations.switchMap(id, this::loadFeesThisYear);
	}

	void setChannel(int channelId) {
		new Thread(() -> {
			id.postValue(channelId);
		}).start();
	}

	LiveData<Double> loadSpentThisMonth(int id) {
		Calendar c = Calendar.getInstance();
		return repo.getSpentAmount(id, c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR));
	}

	LiveData<Double> loadFeesThisYear(int id) {
		Calendar c = Calendar.getInstance();
		return repo.getFees(id, c.get(Calendar.YEAR));
	}

	LiveData<Channel> getChannel() {
		if (channel == null) {
			channel = new MutableLiveData<>();
		}
		return channel;
	}

	LiveData<List<StaxTransaction>> getStaxTransactions() {
		if (transactions == null) {
			transactions = new MutableLiveData<>();
		}
		return transactions;
	}

	LiveData<Double> getSpentThisMonth() {
		if (spentThisMonth == null) {
			spentThisMonth = new MutableLiveData<>();
		}
		return spentThisMonth;
	}

	LiveData<Double> getFeesThisYear() {
		if (feesThisYear == null) { feesThisYear = new MutableLiveData<>(); }
		return feesThisYear;
	}
}


