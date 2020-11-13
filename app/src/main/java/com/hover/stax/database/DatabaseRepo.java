package com.hover.stax.database;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.lifecycle.LiveData;

import com.hover.stax.actions.Action;
import com.hover.stax.actions.ActionDao;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelDao;
import com.hover.stax.requests.Request;
import com.hover.stax.requests.RequestDao;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.schedules.ScheduleDao;
import com.hover.stax.sims.Sim;
import com.hover.stax.sims.SimDao;
import com.hover.stax.transactions.StaxTransaction;
import com.hover.stax.transactions.TransactionDao;
import com.hover.stax.utils.paymentLinkCryptography.Base64;
import com.hover.stax.utils.paymentLinkCryptography.Encryption;

import java.util.List;

public class DatabaseRepo {
	private ChannelDao channelDao;
	private ActionDao actionDao;
	private RequestDao requestDao;
	private ScheduleDao scheduleDao;
	private SimDao simDao;
	private TransactionDao transactionDao;

	private LiveData<List<Channel>> allChannels;
	private LiveData<List<Channel>> selectedChannels;

	public DatabaseRepo(Application application) {
		AppDatabase db = AppDatabase.getInstance(application);
		channelDao = db.channelDao();
		transactionDao = db.transactionDao();
		requestDao = db.requestDao();
		scheduleDao = db.scheduleDao();

		SdkDatabase sdkDb = SdkDatabase.getInstance(application);
		actionDao = sdkDb.actionDao();
		simDao = sdkDb.simDao();

		allChannels = channelDao.getAll();
		selectedChannels = channelDao.getSelected(true);
	}

	// Channels
	public Channel getChannel(int id) {
		return channelDao.getChannel(id);
	}

	public LiveData<Channel> getLiveChannel(int id) {
		return channelDao.getLiveChannel(id);
	}

	public LiveData<List<Channel>> getAll() {
		return allChannels;
	}

	public LiveData<List<Channel>> getSelected() {
		return selectedChannels;
	}

	public void update(Channel channel) {
		AppDatabase.databaseWriteExecutor.execute(() -> channelDao.update(channel));
	}

	// SIMs
	public List<Sim> getSims() {
		return simDao.getPresent();
	}

	// Actions
	public Action getAction(String public_id) {
		return actionDao.getAction(public_id);
	}

	public LiveData<Action> getLiveAction(String public_id) {
		return actionDao.getLiveAction(public_id);
	}

	public LiveData<List<Action>> getLiveActions(int channelId, String type) {
		return actionDao.getLiveActions(channelId, type);
	}

	public LiveData<List<Action>> getLiveActions(int[] channelIds, String type) {
		return actionDao.getActions(channelIds, type);
	}

	public List<Action> getActions(int channelId, String type) {
		return actionDao.getActions(channelId, type);
	}

	// Transactions
	public LiveData<List<StaxTransaction>> getCompleteAndPendingTransferTransactions() {
		return transactionDao.getCompleteAndPendingTransfers();
	}

	public LiveData<List<StaxTransaction>> getCompleteTransferTransactions(int channelId) {
		return transactionDao.getCompleteAndPendingTransfers(channelId);
	}

	@SuppressLint("DefaultLocale")
	public LiveData<Double> getSpentAmount(int channelId, int month, int year) {
		return transactionDao.getTotalAmount(channelId, String.format("%02d", month), String.valueOf(year));
	}

	@SuppressLint("DefaultLocale")
	public LiveData<Double> getFees(int channelId, int year) {
		return transactionDao.getTotalFees(channelId, String.valueOf(year));
	}

	public StaxTransaction getTransaction(String uuid) {
		return transactionDao.getTransaction(uuid);
	}

	public void insert(StaxTransaction transaction) {
		AppDatabase.databaseWriteExecutor.execute(() -> transactionDao.insert(transaction));
	}

	public void update(StaxTransaction transaction) {
		AppDatabase.databaseWriteExecutor.execute(() -> transactionDao.update(transaction));
	}

	// Schedules
	public LiveData<List<Schedule>> getFutureTransactions() {
		return scheduleDao.getLiveFuture();
	}

	public Schedule getSchedule(int id) {
		return scheduleDao.get(id);
	}

	public void insert(Schedule schedule) {
		AppDatabase.databaseWriteExecutor.execute(() -> scheduleDao.insert(schedule));
	}

	public void update(Schedule schedule) {
		AppDatabase.databaseWriteExecutor.execute(() -> scheduleDao.update(schedule));
	}

	public void delete(Schedule schedule) {
		AppDatabase.databaseWriteExecutor.execute(() -> scheduleDao.delete(schedule));
	}

	// Requests
	public LiveData<List<Request>> getRequests() {
		return requestDao.getUnmatched();
	}

	public Encryption.Builder getEncryptionSettings() {
		//ADDING THIS TO REPO, BECAUSE I THINK THESE SETTINGS
		// SHOULDN'T BE IN THE SOURCE CODE, BUT COME FROM AN ONLINE SERVER OR CACHED
		return new Encryption.Builder()
				.setKeyLength(128)
				.setKeyAlgorithm("AES")
				.setCharsetName("UTF8")
				.setIterationCount(65536)
				.setKey("ves€Z€xs€aBKgh")
				.setDigestAlgorithm("SHA1")
				.setSalt("A secured salt")
				.setBase64Mode(Base64.DEFAULT)
				.setAlgorithm("AES/CBC/PKCS5Padding")
				.setSecureRandomAlgorithm("SHA1PRNG")
				.setSecretKeyType("PBKDF2WithHmacSHA1")
				.setIv(new byte[] { 29, 88, -79, -101, -108, -38, -126, 90, 52, 101, -35, 114, 12, -48, -66, -30 });
	}
	public Request getRequest(int id) {
		return requestDao.get(id);
	}

	public void insert(Request request) {
		AppDatabase.databaseWriteExecutor.execute(() -> requestDao.insert(request));
	}

	public void delete(Request request) {
		AppDatabase.databaseWriteExecutor.execute(() -> requestDao.delete(request));
	}

}
