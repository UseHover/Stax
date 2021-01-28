package com.hover.stax.transfers;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.actions.Action;
import com.hover.stax.requests.Request;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.StagedViewModel;

import java.util.List;

public class TransferViewModel extends StagedViewModel {
	final private String TAG = "TransferViewModel";

	private String type = Action.P2P;

	private MediatorLiveData<List<Action>> filteredActions = new MediatorLiveData<>();
	private MediatorLiveData<Action> activeAction = new MediatorLiveData<>();

	private MutableLiveData<String> amount = new MutableLiveData<>();
	private MutableLiveData<StaxContact> contact = new MutableLiveData<>();
	private MutableLiveData<String> note = new MutableLiveData<>();

	private MutableLiveData<Integer> amountError = new MutableLiveData<>();
	private MutableLiveData<Integer> recipientError = new MutableLiveData<>();
	private MutableLiveData<Integer> pageError = new MutableLiveData<>();

	protected LiveData<Request> request = new MutableLiveData<>();

	public TransferViewModel(Application application) {
		super(application);
		activeAction.addSource(filteredActions, this::setActiveActionIfOutOfDate);
	}

	void setType(String transaction_type) {
		type = transaction_type;
	}

	String getType() {
		return type;
	}

//	public void loadActions(Request r) {
//		if (r != null && selectedChannels.getValue() != null && selectedChannels.getValue().size() > 0) {
//			new Thread(() -> {
//				List<Action> actions = repo.getActions(getChannelIds(), r.requester_institution_id);
//				filteredActions.postValue(actions);
//				if (actions.size() <= 0)
//					pageError.postValue(R.string.whoopsie);
//			}).start();
//			activeChannel.addSource(filteredActions, this::setActiveChannel);
//		}
//	}

//	private int[] getChannelIds() {
//		List<Channel> channels = selectedChannels.getValue();
//		int[] ids = new int[channels.size()];
//		for (int c = 0; c < channels.size(); c++)
//			ids[c] = channels.get(c).id;
//		return ids;
//	}

	LiveData<List<Action>> getActions() {
		return filteredActions;
	}

	private void setActiveActionIfOutOfDate(List<Action> actions) {
		if (actions != null && actions.size() > 0 && (activeAction.getValue() == null || !actions.contains(activeAction.getValue())))
			activeAction.setValue(actions.get(0));
	}

	void setActiveAction(Action action) {
		activeAction.postValue(action);
	}
	void setActiveAction(String actionString) {
		if (filteredActions.getValue() == null || filteredActions.getValue().size() == 0) {
			return;
		}
		for (Action a : filteredActions.getValue()) {
			if (a.toString().equals(actionString))
				activeAction.postValue(a);
		}
	}

	void setActiveAction(List<Action> actions) {
		if (actions == null || actions.size() == 0) return;
		activeAction.postValue(actions.get(0));
	}

	LiveData<Action> getActiveAction() {
		if (activeAction == null) {
			activeAction = new MediatorLiveData<>();
		}
		return activeAction;
	}

	void setAmount(String a) {
		amount.postValue(a);
	}

	LiveData<String> getAmount() {
		if (amount == null) {
			amount = new MutableLiveData<>();
		}
		return amount;
	}
	LiveData<Integer> getAmountError() {
		if (amountError == null) { amountError = new MutableLiveData<>(); }
		return amountError;
	}

	void setContact(String contact_ids) {
		if (contact_ids == null) return;
		new Thread(() -> {
			List<StaxContact> contacts = repo.getContacts(contact_ids.split(","));
			if (contacts.size() > 0) contact.postValue(contacts.get(0));
		}).start();
	}

	void setContact(StaxContact c) {
		contact.setValue(c);
	}

	LiveData<StaxContact> getContact() {
		if (contact == null) { contact = new MutableLiveData<>(); }
		return contact;
	}

	void setRecipient(String r) {
		if (contact.getValue() != null && contact.getValue().toString().equals(r)) { return; }
		contact.setValue(new StaxContact(r));
	}

	LiveData<Integer> getRecipientError() {
		if (recipientError == null) { recipientError = new MutableLiveData<>(); }
		return recipientError;
	}

	LiveData<Integer> getPageError() {
		if (pageError == null) { pageError = new MutableLiveData<>(); }
		return pageError;
	}

	public LiveData<Schedule> getSchedule() {
		if (schedule == null) { schedule = new MutableLiveData<>(); }
		return schedule;
	}

	public LiveData<Request> getRequest() {
		if (request == null) { request = new MutableLiveData<>(); }
		return request;
	}

	void setNote(String r) {
		note.postValue(r);
	}

	LiveData<String> getNote() {
		if (note == null) {
			note = new MutableLiveData<>();
			note.setValue(" ");
		}
		return note;
	}

	boolean requiresActionChoice() { // in last case, should have request network as choice
		return filteredActions.getValue() != null && filteredActions.getValue().size() > 0 && (filteredActions.getValue().size() > 1 || filteredActions.getValue().get(0).hasDiffToInstitution());
	}

	boolean hasActionsLoaded() {
		return filteredActions.getValue() != null && filteredActions.getValue().size() > 0 &&
					   (filteredActions.getValue().size() > 1 || (activeAction.getValue() != null && activeAction.getValue().hasToInstitution()));
	}

	public void decrypt(String encryptedLink) {
		request = repo.decrypt(encryptedLink, getApplication());
//		filteredActions.addSource(request, this::loadActions);
	}

	public void view(Request r) {
		setAmount(r.amount);
		setRecipient(r.requester_number);
	}

	public void view(Schedule s) {
		schedule.setValue(s);
		setType(s.type);
//		setActiveChannel(s.channel_id);
		setAmount(s.amount);
		setContact(s.recipient_ids);
		setNote(s.note);
	}

	public void checkSchedule() {
		if (schedule.getValue() != null) {
			Schedule s = schedule.getValue();
			if (s.end_date <= DateUtils.today()) {
				s.complete = true;
				repo.update(s);
			}
		}
	}

	public void saveContact() {
		if (contact.getValue() != null) {
			new Thread(() -> {
				StaxContact c = contact.getValue();
				c.lastUsedTimestamp = DateUtils.now();
				repo.insertOrUpdate(contact.getValue());
			}).start();
		}
	}
}