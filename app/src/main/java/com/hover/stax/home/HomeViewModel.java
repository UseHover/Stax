package com.hover.stax.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.api.Hover;
import com.hover.sdk.sims.SimInfo;
import com.hover.sdk.transactions.Transaction;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.database.KeyStoreExecutor;
import com.hover.stax.utils.Utils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
	private LiveData<List<Channel>> selectedChannels;
	private MutableLiveData<List<BalanceModel>> balances;

	private final LiveData<List<Action>> actions;
	private DatabaseRepo repo;


	public HomeViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);
		balances = new MutableLiveData<>();
		selectedChannels = new MutableLiveData<>();
		selectedChannels = repo.getSelected();

		actions = repo.getActions();

		balances.setValue(new ArrayList<>());

	}

	public  LiveData<List<Channel>> loadChannels() {return selectedChannels; }
	public LiveData<List<BalanceModel>> loadBalance() {
		return balances;
	}
	public LiveData<List<Action>> loadActions() {
		return actions;
	}

	public void getBalanceFunction(List<Channel> channels) {
		List<HoverAction> balanceActions = repo.getActionsWithBalanceType();
		ArrayList<BalanceModel> balanceModelList = new ArrayList<>();

		if ( balanceActions != null) {
			List<String> simHniList = new ArrayList<>();
			for (SimInfo sim : Hover.getPresentSims(ApplicationInstance.getContext())) {
				if (!simHniList.contains(sim.getOSReportedHni()))
					simHniList.add(sim.getOSReportedHni());
			}

			List<Channel> selectedChannelInSIM = Utils.getSimChannels(channels, simHniList);


			for (Channel channel : selectedChannelInSIM) {
				for (HoverAction action : balanceActions) {
					if (action.channelId == channel.id) {
						if (channel.pin != null && channel.pin.length() > 30) 
							channel.pin = KeyStoreExecutor.decrypt(channel.pin,ApplicationInstance.getContext());

						List<Transaction> transactionList = Hover.getTransactionsByActionId(action.id, ApplicationInstance.getContext());
						String balanceValue = "NaN";
						long timeStamp = 0;
						if (transactionList.size() > 0) {
							Transaction mostRecentTransaction = transactionList.get(0);
							timeStamp = mostRecentTransaction.updatedTimestamp;
							try {
								balanceValue = mostRecentTransaction.parsed_variables.getString("balance");
							} catch (NullPointerException | JSONException e) {
								e.printStackTrace();
							}
						}
						balanceModelList.add(new BalanceModel(action.id, channel, balanceValue, timeStamp));

					}
				}
			}
		}

		balances.postValue(balanceModelList);
	}
}