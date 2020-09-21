package com.hover.stax.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.hover.stax.actions.Action;
import com.hover.stax.actions.ActionDao;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelDao;
import com.hover.stax.sims.Sim;
import com.hover.stax.sims.SimDao;
import com.hover.stax.transactions.StaxTransaction;
import com.hover.stax.transactions.TransactionDao;

import java.util.List;

public class DatabaseRepo {
	private ChannelDao channelDao;
	private ActionDao actionDao;
	private SimDao simDao;
	private TransactionDao transactionDao;

	private LiveData<List<Channel>> allChannels;
	private LiveData<List<Channel>> selectedChannels;
	private Channel defaultChannel;

	public DatabaseRepo(Application application) {
		AppDatabase db = AppDatabase.getInstance(application);
		channelDao = db.channelDao();
		transactionDao = db.transactionDao();

		SdkDatabase sdkDb = SdkDatabase.getInstance(application);
		actionDao = sdkDb.actionDao();
		simDao = sdkDb.simDao();

		allChannels = channelDao.getAll();
		selectedChannels = channelDao.getSelected(true);
	}

	// Room executes all queries on a separate thread.
// Observed LiveData will notify the observer when the data has changed.
	public Channel getChannel(int id) {
		return channelDao.getChannel(id);
	}

	public LiveData<Channel> getChannelV2(int id) {
		return channelDao.getChannelV2(id);
	}

	public LiveData<List<Channel>> getAll() {
		return allChannels;
	}

	public LiveData<List<Channel>> getSelected() {
		return selectedChannels;
	}

	public LiveData<Channel> getDefault() {
		return channelDao.getDefault();
	}

	public void insert(Channel channel) {
		AppDatabase.databaseWriteExecutor.execute(() -> channelDao.insert(channel));
	}

	public void update(Channel channel) {
		AppDatabase.databaseWriteExecutor.execute(() -> channelDao.update(channel));
	}

	public LiveData<List<Sim>> getSims() { return simDao.getPresent(); }

	public Action getAction(String public_id) {
		return actionDao.getAction(public_id);
	}

	public LiveData<Action> getLiveAction(String public_id) {
		return actionDao.getLiveAction(public_id);
	}

	public LiveData<List<Action>> getActions(int channelId, String type) {
		return actionDao.getActions(channelId, type);
	}

	public LiveData<List<Action>> getActions(int[] channelIds, String type) {
		return actionDao.getActions(channelIds, type);
	}

	public LiveData<List<Action>> getActions() {
		return actionDao.getAll();
	}

	public LiveData<List<StaxTransaction>> getCompleteTransferTransactions() {
		return transactionDao.getNonBalance();
	}

	public LiveData<List<StaxTransaction>> getCompleteTransferTransactionsByChannelId(int channelId) {
		return transactionDao.getChannelTransactions(channelId);
	}
	public LiveData<Double> getSpentAmount(int channelId, int month, int year) {
		return  transactionDao.getTotalAmount(channelId, month, year);
	}

	public StaxTransaction getTransaction(String uuid) {
		return transactionDao.getTransaction(uuid);
	}

	public LiveData<StaxTransaction> getLiveTransaction(String uuid) {
		return transactionDao.getLiveTransaction(uuid);
	}

	public void insert(StaxTransaction transaction) {
		AppDatabase.databaseWriteExecutor.execute(() -> transactionDao.insert(transaction));
	}

	public void update(StaxTransaction transaction) {
		AppDatabase.databaseWriteExecutor.execute(() -> transactionDao.update(transaction));
	}
}
