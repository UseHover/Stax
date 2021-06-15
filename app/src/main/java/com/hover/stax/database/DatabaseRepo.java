package com.hover.stax.database;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.actions.HoverActionDao;
import com.hover.sdk.database.HoverRoomDatabase;
import com.hover.sdk.sims.SimInfo;
import com.hover.sdk.sims.SimInfoDao;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelDao;
import com.hover.stax.contacts.ContactDao;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.requests.Request;
import com.hover.stax.requests.RequestDao;
import com.hover.stax.requests.Shortlink;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.schedules.ScheduleDao;
import com.hover.stax.transactions.StaxTransaction;
import com.hover.stax.transactions.TransactionDao;
import com.hover.stax.utils.Utils;
import com.hover.stax.utils.paymentLinkCryptography.Encryption;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public class DatabaseRepo {
    private static String TAG = "DatabaseRepo";
    private ChannelDao channelDao;
    private HoverActionDao actionDao;
    private RequestDao requestDao;
    private ScheduleDao scheduleDao;
    private SimInfoDao simDao;
    private TransactionDao transactionDao;
    private ContactDao contactDao;

    private LiveData<List<Channel>> allChannels;
    private LiveData<List<Channel>> selectedChannels;

    private MutableLiveData<Request> decryptedRequest = new MutableLiveData<>();

    public DatabaseRepo(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        channelDao = db.channelDao();
        transactionDao = db.transactionDao();
        contactDao = db.contactDao();
        requestDao = db.requestDao();
        scheduleDao = db.scheduleDao();

        HoverRoomDatabase sdkDb = HoverRoomDatabase.getInstance(application);
        actionDao = sdkDb.actionDao();
        simDao = sdkDb.simDao();

        allChannels = channelDao.getAllInAlphaOrder();
        selectedChannels = channelDao.getSelected(true);
    }

    // Channels
    public Channel getChannel(int id) {
        return channelDao.getChannel(id);
    }

    public LiveData<Channel> getLiveChannel(int id) {
        return channelDao.getLiveChannel(id);
    }

    public LiveData<List<Channel>> getAllChannels() {
        return allChannels;
    }

    public LiveData<List<Channel>> getChannels(int[] ids) {
        return channelDao.getChannels(ids);
    }

    public LiveData<List<Channel>> getChannelsByCountry(int[] channelIds, String countryCode) {
        return channelDao.getChannels(countryCode, channelIds);
    }

    public LiveData<List<Channel>> getSelected() {
        return selectedChannels;
    }

    public void update(Channel channel) {
        AppDatabase.databaseWriteExecutor.execute(() -> channelDao.update(channel));
    }

    // SIMs
    public List<SimInfo> getPresentSims() {
        return simDao.getPresent();
    }

    public List<SimInfo> getSims(String[] hnis) {
        return simDao.getPresentByHnis(hnis);
    }

    // Actions
    public HoverAction getAction(String public_id) {
        return actionDao.getAction(public_id);
    }

    public LiveData<HoverAction> getLiveAction(String public_id) {
        return actionDao.getLiveAction(public_id);
    }

    public LiveData<List<HoverAction>> getLiveActions(int[] channelIds, String type) {
        return actionDao.getLiveActions(channelIds, type);
    }

    public List<HoverAction> getTransferActions(int channelId) {
        return actionDao.getTransferActions(channelId);
    }

    public List<HoverAction> getActions(int channelId, String type) {
        return actionDao.getActions(channelId, type);
    }

    public List<HoverAction> getActions(int[] channelIds, String type) {
        return actionDao.getActions(channelIds, type);
    }

    public List<HoverAction> getActions(int[] channelIds, int recipientInstitutionId) {
        return actionDao.getActions(channelIds, recipientInstitutionId, HoverAction.P2P);
    }

    public LiveData<List<HoverAction>> getBountyActions() {
        return actionDao.getBountyActions();
    }

    // Transactions
    public LiveData<List<StaxTransaction>> getCompleteAndPendingTransferTransactions() {
        return transactionDao.getCompleteAndPendingTransfers();
    }
    public LiveData<List<StaxTransaction>> getTransactionsForAppReview() {
        return transactionDao.getTransactionsForAppReview();
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
                StaxContact contact = intent.hasExtra(StaxContact.LOOKUP_KEY) ? getContact(intent.getStringExtra(StaxContact.LOOKUP_KEY)) : null;
                HoverAction a = intent.hasExtra(HoverAction.ID_KEY) ? getAction(intent.getStringExtra(HoverAction.ID_KEY)) : null;

                if (t == null) {
                    Utils.logAnalyticsEvent(c.getString(R.string.initializing_USSD_event_confirmed), c);
                    t = new StaxTransaction(intent, a, contact, c);
                    transactionDao.insert(t);
                }
                t.update(intent, a, contact, c);
                transactionDao.update(t);

                updateRequests(t, intent);
            } catch (Exception e) {
                Log.e(TAG, "error", e);
            }
        });
    }

    private void updateRequests(StaxTransaction t, Intent intent) {
        if (t.transaction_type.equals(HoverAction.RECEIVE)) {
            List<Request> rs = getRequests();
            for (Request r : rs) {
                StaxContact r_contact = getContact(r.requestee_ids);
                if (r_contact != null && r_contact.equals(new StaxContact(intent.getStringExtra("senderPhone")))) {
                    r.matched_transaction_uuid = t.uuid;
                    update(r);
                }
            }
        }
    }

    // Contacts
    public LiveData<List<StaxContact>> getAllContacts() {
        return contactDao.getAll();
    }

    public List<StaxContact> getContacts(String[] ids) {
        return contactDao.get(ids);
    }

    public LiveData<List<StaxContact>> getLiveContacts(String[] ids) {
        return contactDao.getLive(ids);
    }

    public StaxContact lookupContact(String lookupKey) {
        return contactDao.lookup(lookupKey);
    }

    public StaxContact getContact(String lookupKey) {
        return contactDao.lookup(lookupKey);
    }

    public LiveData<StaxContact> getLiveContact(String id) {
        return contactDao.getLive(id);
    }

    public StaxContact getContactFromPhone(String phone) {
        return contactDao.getContact(phone);
    }

    public void insertOrUpdate(StaxContact contact) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (getContact(contact.id) == null) {
                try {
                    contactDao.insert(contact);
                } catch (Exception e) {
                    Utils.logErrorAndReportToFirebase(TAG, "failed to insert contact", e);
                }
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

    public LiveData<List<Schedule>> getFutureTransactions(int channelId) {
        return scheduleDao.getLiveFutureByChannelId(channelId);
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
    public LiveData<List<Request>> getLiveRequests() {
        return requestDao.getLiveUnmatched();
    }
    public LiveData<List<Request>> getLiveRequests(int channelId) {
        return requestDao.getLiveUnmatchedByChannel(channelId);
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
        String removedBaseUrlString = encrypted.replace(c.getString(R.string.payment_root_url, ""), "");

        //Only old stax versions contains ( in the link
        if (removedBaseUrlString.contains("(")) decryptRequestForOldVersions(removedBaseUrlString);
        else decryptRequest(removedBaseUrlString, c);
        return decryptedRequest;
    }

    private void decryptRequest(String param, Context c) {
        decryptedRequest.postValue(new Request(Request.decryptBijective(param, c)));
    }
    private void decryptRequestForOldVersions(String params) {
        try {
            Encryption e = Request.getEncryptionSettings().build();
            if (Request.isShortLink(params)) {
                params = new Shortlink(params).expand();
            }

            e.decryptAsync(params.replaceAll("[(]", "+"), new Encryption.Callback() {
                @Override
                public void onSuccess(String result) {
                    decryptedRequest.postValue(new Request(result));
                }

                @Override
                public void onError(Exception exception) {
                    Utils.logErrorAndReportToFirebase(TAG, "failed link decryption", exception);
                }
            });

        } catch (NoSuchAlgorithmException e) {
            Utils.logErrorAndReportToFirebase(TAG, "decryption failure", e);
        }
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
