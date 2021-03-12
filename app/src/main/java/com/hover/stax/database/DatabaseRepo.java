package com.hover.stax.database;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.R;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.actions.Action;
import com.hover.stax.actions.ActionDao;
import com.hover.stax.bounty.BountyUser;
import com.hover.stax.bounty.BountyUserDao;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelDao;
import com.hover.stax.contacts.ContactDao;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.requests.Request;
import com.hover.stax.requests.RequestDao;
import com.hover.stax.requests.Shortlink;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.schedules.ScheduleDao;
import com.hover.stax.sims.Sim;
import com.hover.stax.sims.SimDao;
import com.hover.stax.transactions.StaxTransaction;
import com.hover.stax.transactions.TransactionDao;
import com.hover.stax.utils.Utils;
import com.hover.stax.utils.paymentLinkCryptography.Encryption;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public class DatabaseRepo {
	private static String TAG = "DatabaseRepo";
	private ChannelDao channelDao;
	private ActionDao actionDao;
	private RequestDao requestDao;
	private ScheduleDao scheduleDao;
	private SimDao simDao;
	private TransactionDao transactionDao;
	private ContactDao contactDao;
	private BountyUserDao bountyUserDao;

	private LiveData<List<Channel>> allChannels;
	private LiveData<List<Channel>> allChannelsBySelected;
	private LiveData<List<Channel>> selectedChannels;
	private MediatorLiveData<List<Action>> filteredActions = new MediatorLiveData<>();

	private MutableLiveData<Request> decryptedRequest= new MutableLiveData<>();

	public DatabaseRepo(Application application) {
		AppDatabase db = AppDatabase.getInstance(application);
		channelDao = db.channelDao();
		transactionDao = db.transactionDao();
		contactDao = db.contactDao();
		requestDao = db.requestDao();
		scheduleDao = db.scheduleDao();
		bountyUserDao = db.bountyDao();

		SdkDatabase sdkDb = SdkDatabase.getInstance(application);
		actionDao = sdkDb.actionDao();
		simDao = sdkDb.simDao();

		allChannels = channelDao.getAllInAlphaOrder();
		allChannelsBySelected = channelDao.getAllInSelectedOrder();
		selectedChannels = channelDao.getSelected(true);
	}

	// Channels
	public Channel getChannel(int id) {
		return channelDao.getChannel(id);
	}
	public Channel getChannelByInstitutionId(int id) { return channelDao.getChannelByInstitutionId(id); }

	public LiveData<Channel> getLiveChannel(int id) {
		return channelDao.getLiveChannel(id);
	}

	public LiveData<List<Channel>> getAllChannels() {
		return allChannels;
	}
	public LiveData<List<Channel>> getAllChannelsBySelectedOrder() {
		return allChannelsBySelected;
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
		return actionDao.getLiveActions(channelIds, type);
	}

	public List<Action> getTransferActions(int channelId) {
		return actionDao.getTransferActions(channelId);
	}

	public List<Action> getActions(int channelId, String type) {
		return actionDao.getActions(channelId, type);
	}

	public List<Action> getActions(int[] channelIds, String type) {
		return actionDao.getActions(channelIds, type);
	}

	public List<Action> getActions(int[] channelIds, int recipientInstitutionId) {
		return actionDao.getActions(channelIds, recipientInstitutionId, Action.P2P);
	}
	public List<Action> getActionsForBounty() {
		return actionDao.getAllActionsForBounty();
	}
	public LiveData<List<Action>> getLiveActionsForBounty() {
		return actionDao.getAllLiveActionsForBounty();
	}

	// Transactions
	public LiveData<List<StaxTransaction>> getCompleteAndPendingTransferTransactions() {
		return transactionDao.getCompleteAndPendingTransfers();
	}

	public LiveData<List<StaxTransaction>> getBountyTransactions() {
		return transactionDao.getBountyTransactions();
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

	public void insertOrUpdateTransaction(Intent intent, Context c) {
		AppDatabase.databaseWriteExecutor.execute(() -> {
			try {
				StaxTransaction t = getTransaction(intent.getStringExtra(TransactionContract.COLUMN_UUID));
				StaxContact contact = intent.hasExtra(StaxContact.ID_KEY) ? getContact(intent.getStringExtra(StaxContact.ID_KEY)) : null;
				Action a = intent.hasExtra(Action.ID_KEY) ? getAction(intent.getStringExtra(Action.ID_KEY)) : null;

				if (t == null) {
					t = new StaxTransaction(intent, a, contact, c);
					transactionDao.insert(t);
				}
				t.update(intent, a, contact, c);
				transactionDao.update(t);

				updateRequests(t, intent);
			} catch (Exception e) { Log.e(TAG, "error", e); }
		});
	}

	private void updateRequests(StaxTransaction t, Intent intent) {
		if (t.transaction_type.equals(Action.RECEIVE)) {
			List<Request> rs = getRequests();
			for (Request r: rs) {
				StaxContact r_contact = getContact(r.requestee_ids);
				if (r_contact != null && r_contact.equals(new StaxContact(intent.getStringExtra("senderPhone")))) {
					r.matched_transaction_uuid = t.uuid;
					update(r);
				}
			}
		}
	}

	// Contacts
	public LiveData<List<StaxContact>> getAllContacts() { return contactDao.getAll(); }

	public List<StaxContact> getContacts(String[] ids) { return contactDao.get(ids); }
	public LiveData<List<StaxContact>> getLiveContacts(String[] ids) { return contactDao.getLive(ids); }

	public StaxContact lookupContact(String lookupKey) { return contactDao.lookup(lookupKey); }
	public StaxContact getContact(String lookupKey) { return contactDao.lookup(lookupKey); }
	public LiveData<StaxContact> getLiveContact(String id) { return contactDao.getLive(id); }

	public void insertOrUpdate(StaxContact contact) {
		AppDatabase.databaseWriteExecutor.execute(() -> {
			if (getContact(contact.id) == null) {
				try { contactDao.insert(contact); }
				catch (Exception e) { Utils.logErrorAndReportToFirebase(TAG, "failed to insert contact", e); }
			} else
				contactDao.update(contact);
		});
	}

	public void update(StaxContact contact) {
		AppDatabase.databaseWriteExecutor.execute(() -> contactDao.update(contact));
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

	//Bounty user
	public Integer getBountyUserCount() {return bountyUserDao.getEntriesCount();}
	public void insert(BountyUser bountyUser) {
		AppDatabase.databaseWriteExecutor.execute(()->bountyUserDao.insert(bountyUser));
	}
	public void update(BountyUser bountyUser) {
		AppDatabase.databaseWriteExecutor.execute(()->bountyUserDao.insert(bountyUser));
	}

	// Requests
	public LiveData<List<Request>> getLiveRequests() {
		return requestDao.getLiveUnmatched();
	}

	public List<Request> getRequests() {
		return requestDao.getUnmatched();
	}

	public Request getRequest(int id) {
		return requestDao.get(id);
	}

	public LiveData<Request> decrypt(String encrypted, Context c) {
		if (decryptedRequest == null) { decryptedRequest = new MutableLiveData<>(); }
		decryptedRequest.setValue(null);
		try {
			Encryption e = Request.getEncryptionSettings().build();

			String removedBaseUrlString =  encrypted.replace(c.getString(R.string.payment_root_url, ""),"");
			if (Request.isShortLink(removedBaseUrlString)) {
				removedBaseUrlString = new Shortlink(removedBaseUrlString).expand();
			}

			e.decryptAsync(removedBaseUrlString.replaceAll("[(]","+"), new Encryption.Callback() {
				@Override public void onSuccess(String result) {
					decryptedRequest.postValue(new Request(result));
				}
				@Override public void onError(Exception exception) {
					Utils.logErrorAndReportToFirebase(TAG, "failed link decryption", exception);}
			});

		} catch (NoSuchAlgorithmException e) { Utils.logErrorAndReportToFirebase(TAG, "decryption failure", e); }
		return decryptedRequest;
	}

	public void insert(Request request) {
		AppDatabase.databaseWriteExecutor.execute(() -> requestDao.insert(request));
	}

	public void update(Request request) {
		AppDatabase.databaseWriteExecutor.execute(() -> requestDao.update(request));
	}

	public void delete(Request request) {
		AppDatabase.databaseWriteExecutor.execute(() -> requestDao.delete(request));
	}

}
