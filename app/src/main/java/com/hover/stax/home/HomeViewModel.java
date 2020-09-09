package com.hover.stax.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.api.Hover;
import com.hover.sdk.transactions.Transaction;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.models.StaxDate;
import com.hover.stax.utils.Utils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class HomeViewModel extends AndroidViewModel {
	private LiveData<List<Channel>> selectedChannels;
	private LiveData<List<Action>> balanceActions;
	private MutableLiveData<List<Transaction>> transactions;

	private DatabaseRepo repo;

	public HomeViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		selectedChannels = repo.getSelected();
		balanceActions = Transformations.switchMap(selectedChannels, this::loadBalanceActions);
		transactions = new MutableLiveData<>();
		transactions.setValue(Hover.getAllTransactions(ApplicationInstance.getContext()));
	}

	LiveData<List<Transaction>> getTransactions() {return  transactions;}


	MutableLiveData<List<StaxTransactionModel>> getTransactionModels(List<Transaction> transactionList) {
		List<StaxTransactionModel> staxTransactionModels = new ArrayList<>();
		String lastTime = "";
		for(Transaction transaction : transactionList) {
			Action action1 = repo.getAction(transaction.actionId);
			if(!action1.transaction_type.equals("balance")) {

				Channel channel = repo.getChannel(action1.channel_id);
				String amount = null;
				try {
					amount = "-"+Utils.formatAmount(transaction.parsed_variables.getString("amount"));
				} catch (Exception ignored) { }
				if(amount !=null) {
					StaxTransactionModel staxTransactionModel = new StaxTransactionModel();
					staxTransactionModel.setActionId(action1.public_id);
					staxTransactionModel.setAmount(amount);
					staxTransactionModel.setChannelId(channel.id);
					staxTransactionModel.setChannelName(channel.name);
					staxTransactionModel.setTransactionUUIDId(transaction.uuid);
					staxTransactionModel.setToTransactionType(Utils.getTransactionTypeFullString(action1.transaction_type));

					StaxDate staxDate = Utils.getStaxDate(transaction.updatedTimestamp);
					staxTransactionModel.setStaxDate(staxDate);

					String concatenatedDate = staxDate.getYear()+staxDate.getMonth()+staxDate.getDayOfMonth();
					if(!lastTime.equals(concatenatedDate)) staxTransactionModel.setShowDate(true);
					else staxTransactionModel.setShowDate(false);
					lastTime = concatenatedDate;

					staxTransactionModels.add(staxTransactionModel);
				}


			}
		}
		MutableLiveData<List<StaxTransactionModel>> modeLiveData = new MutableLiveData<>();
		modeLiveData.setValue(staxTransactionModels);
		return  modeLiveData;
	}

	public LiveData<List<Action>> loadBalanceActions(List<Channel> channelList) {
		int[] ids = new int[channelList.size()];
		for (int c = 0; c < channelList.size(); c++) {
			ids[c] = channelList.get(c).id;
		}
		return repo.getActions(ids, "balance");
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
		return repo.getActions(channel_id, "balance");
	}

}