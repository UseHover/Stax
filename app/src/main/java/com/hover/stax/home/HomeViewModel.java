package com.hover.stax.home;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.sdk.api.Hover;
import com.hover.sdk.transactions.Transaction;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
	private final String TAG = "HomeViewModel";

	private LiveData<List<Channel>> selectedChannels;
	private LiveData<List<Action>> balanceActions;
	private MutableLiveData<List<Transaction>> transactions;

	private LiveData<List<StaxTransaction>> staxTransactions;

	private DatabaseRepo repo;

	public HomeViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		selectedChannels = repo.getSelected();
		balanceActions = Transformations.switchMap(selectedChannels, this::loadBalanceActions);

		transactions = new MutableLiveData<>();
		updateTransactions();
		staxTransactions = Transformations.switchMap(transactions, this::getTransactionModels);
	}

	public void updateTransactions() { transactions.setValue(Hover.getAllTransactions(getApplication())); }

	LiveData<List<StaxTransaction>> getTransactionModels(List<Transaction> transactionList) {
		List<StaxTransaction> staxTransactions = new ArrayList<>();
		String lastTime = "";
		for (Transaction transaction : transactionList) {
			if (!transaction.myType.equals(Action.BALANCE) && transaction.parsed_variables != null && transaction.parsed_variables.has(Action.AMOUNT_KEY)) {
				try {
					StaxTransaction staxTransaction = new StaxTransaction(transaction, lastTime, getApplication());
					lastTime = staxTransaction.getDateString();
					staxTransactions.add(staxTransaction);
				} catch (JSONException e) { Log.e(TAG, "Error parsing transaction", e); }
			}
		}
		MutableLiveData<List<StaxTransaction>> modeLiveData = new MutableLiveData<>();
		modeLiveData.setValue(staxTransactions);
		return modeLiveData;
	}

	public LiveData<List<StaxTransaction>> getStaxTranssactions() {
		return staxTransactions;
	}

	public LiveData<List<Action>> loadBalanceActions(List<Channel> channelList) {
		int[] ids = new int[channelList.size()];
		for (int c = 0; c < channelList.size(); c++) {
			ids[c] = channelList.get(c).id;
		}
		return repo.getActions(ids, Action.BALANCE);
	}

	public LiveData<List<Channel>> getSelectedChannels() {
		return selectedChannels;
	}

	public Channel getChannel(int id) {
		List<Channel> allChannels = selectedChannels.getValue() != null ? selectedChannels.getValue() : new ArrayList<>();
		for (Channel channel : allChannels) {
			if (channel.id == id) {
				return channel;
			}
		}
		return null;
	}

	public LiveData<List<Action>> getBalanceActions() {
		return balanceActions;
	}

	public LiveData<List<Action>> getBalanceAction(int channel_id) {
		return repo.getActions(channel_id, Action.BALANCE);
	}

}