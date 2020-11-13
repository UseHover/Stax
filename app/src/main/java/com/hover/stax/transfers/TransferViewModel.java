package com.hover.stax.transfers;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.Constants;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.StagedViewModel;
import com.hover.stax.utils.Utils;
import com.hover.stax.utils.paymentLinkCryptography.Encryption;

import java.util.ArrayList;
import java.util.List;

import static com.hover.stax.transfers.TransferStage.REVIEW;
import static com.hover.stax.transfers.TransferStage.REVIEW_DIRECT;

public class TransferViewModel extends StagedViewModel {

	private String type = Action.P2P;
	private LiveData<List<Channel>> selectedChannels = new MutableLiveData<>();

	private MediatorLiveData<Channel> activeChannel = new MediatorLiveData<>();
	private LiveData<List<Action>> filteredActions = new MutableLiveData<>();
	private MediatorLiveData<Action> activeAction = new MediatorLiveData<>();
	private MediatorLiveData<Boolean> ischannelRelationshipExistMediator = new MediatorLiveData<>();

	private MutableLiveData<String> amount = new MutableLiveData<>();
	private MutableLiveData<String> activeActionName = new MutableLiveData<>();
	private MutableLiveData<String> recipient = new MutableLiveData<>();
	private MutableLiveData<String> note = new MutableLiveData<>();

	private MutableLiveData<Integer> amountError = new MutableLiveData<>();
	private MutableLiveData<Integer> recipientError = new MutableLiveData<>();
	private MutableLiveData<Boolean> ischannelRelationshipExist = new MutableLiveData<>();
	private MutableLiveData<CustomizedTransferSummarySettings>ctssLiveData = new MutableLiveData<>();

	private boolean isFromStaxLink = false;
	private int channelIdFromStaxLink = 0;


	public TransferViewModel(Application application) {
		super(application);
		stage.setValue(TransferStage.AMOUNT);

		selectedChannels = repo.getSelected();
		activeChannel.addSource(selectedChannels, this::findActiveChannel);
		filteredActions = Transformations.switchMap(activeChannel, this::loadActions);
		activeAction.addSource(filteredActions, this::findActiveAction);
		ischannelRelationshipExistMediator.addSource(filteredActions, this::checkIfRelationshipExistForStaxLinkSake);
	}


	void setType(String transaction_type) {
		type = transaction_type;
	}

	String getType() {
		return type;
	}

	public LiveData<CustomizedTransferSummarySettings> getCtssLiveData() {
		return ctssLiveData;
	}

	private void findActiveChannel(List<Channel> channels) {
		if (channels != null && channels.size() > 0) {
			activeChannel.setValue(channels.get(0));
		}
	}

	void setActiveChannel(String channelString) {
		if (selectedChannels.getValue() == null || selectedChannels.getValue().size() == 0) {
			return;
		}
		for (Channel c : selectedChannels.getValue()) {
			if (c.toString().equals(channelString))
				activeChannel.setValue(c);
		}
	}

	void setActiveChannel(int channel_id) {
		if (selectedChannels.getValue() == null || selectedChannels.getValue().size() == 0) {
			return;
		}
		for (Channel c : selectedChannels.getValue()) {
			if (c.id == channel_id)
				activeChannel.setValue(c);
		}
	}

	LiveData<Channel> getActiveChannel() {
		return activeChannel;
	}

	public LiveData<List<Action>> loadActions(Channel channel) {
		if (channel != null) {
			filteredActions = repo.getLiveActions(channel.id, type);
		}
		return filteredActions;
	}

	public void checkIfRelationshipExistForStaxLinkSake(List<Action> filteredActions) {
		if(isFromStaxLink) {
			//CHECK IF THE SENDER'S SELECTED CHANNEL HAS AN ACTION WITH A LINK TO THE RECIPIENT'S CHANNEL ID.
			if(filteredActions !=null) {
				if(filteredActions.size() == 0) {
					//Spent hours trying to debug this, for whatever reason, the actions kept returning empty until when reloaded
					//Or when user clicks on another channel radio button option.
					activeActionName.postValue(null);
					new Thread(()-> {
						List<Action> acts = repo.getActions(activeChannel.getValue().id, type);
						checkIfRelationshipExistForStaxLinkSake(acts);
					}).start();

				}else {
					boolean linkChecked = false;
					for (Action action : filteredActions) {
						if (action.channel_id == channelIdFromStaxLink) {
							linkChecked = true;
							setActiveAction(action);
							break;
						}
					}
					ischannelRelationshipExist.postValue(linkChecked);
				}
			}
		}
	}

	public LiveData<Boolean> getIschannelRelationshipExist() {
		return ischannelRelationshipExist;
	}

	public LiveData<Boolean> getIschannelRelationshipExistMediator() {
		return ischannelRelationshipExistMediator;
	}

	LiveData<List<Channel>> getSelectedChannels() {
		return selectedChannels;
	}

	LiveData<List<Action>> getActions() {
		return filteredActions;
	}

	private void findActiveAction(List<Action> actions) {
		if (actions != null && actions.size() > 0 && activeAction.getValue() == null) {
			activeAction.setValue(actions.get(0));
		}
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
				activeAction.setValue(a);
		}
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

	void setRecipient(String r) {
		recipient.postValue(r);
	}

	LiveData<String> getRecipient() {
		if (recipient == null) {
			recipient = new MutableLiveData<>();
		}
		return recipient;
	}
	LiveData<Integer> getRecipientError() {
		if (recipientError == null) { recipientError = new MutableLiveData<>(); }
		return recipientError;
	}


	LiveData<String> forceGetActionName() {
		return  activeActionName;
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

	@Override
	public void goToNextStage() {
		stage.postValue(goToNextStage(stage.getValue()));
	}

	private StagedEnum goToNextStage(StagedEnum currentStage) {
		StagedEnum next = currentStage.next();
		if (!stageRequired((TransferStage) next))
			next = goToNextStage(next);
		return next;
	}

	boolean stageRequired(TransferStage ts) {
		switch (ts) {
			case TO_NETWORK:
				return requiresActionChoice();
			case RECIPIENT:
				return activeAction.getValue() != null && activeAction.getValue().requiresRecipient();
			case NOTE:
				return activeAction.getValue() != null && activeAction.getValue().requiresReason();
			default:
				return true;
		}
	}

	boolean requiresActionChoice() { // in last case, should have request network as choice
		return filteredActions.getValue() != null && filteredActions.getValue().size() > 0 && (filteredActions.getValue().size() > 1 || filteredActions.getValue().get(0).hasDiffToInstitution());
	}

	boolean stageValidates() {
		if (stage.getValue() == null) return false;
		switch ((TransferStage) stage.getValue()) {
			case AMOUNT:
				if (amount.getValue() == null || amount.getValue().isEmpty()) {
					amountError.setValue(R.string.amount_fielderror);
					return false;
				} else
					amountError.setValue(null);
				break;

			case FROM_ACCOUNT:
				if(isFromStaxLink && (ischannelRelationshipExist.getValue() ==null || !ischannelRelationshipExist.getValue())) {
					if(activeActionName.getValue() != null) ischannelRelationshipExist.postValue(false);
					return false;
				}
				break;

			case RECIPIENT:
				if (recipient.getValue() == null || recipient.getValue().isEmpty()) {
					recipientError.setValue(R.string.recipient_fielderror);
					return false;
				} else
					recipientError.setValue(null);
				break;
		}
		return true;
	}

	void setupTransferPageFromPaymentLink(String encryptedString) {
		try{
			Encryption encryption = repo.getEncryptionSettings().build();
			encryption.decryptAsync(encryptedString.replace(Constants.STAX_LINK_PREFIX,""), new Encryption.Callback() {
				@Override
				public void onSuccess(String result) {
					isFromStaxLink = true;
					String separator = Constants.PAYMENT_LINK_SEPERATOR;
					String[] splittedString = result.split(separator);
					String amount = Utils.formatAmount(splittedString[0]);
					int channel_id = Integer.parseInt(splittedString[1]);
					channelIdFromStaxLink = channel_id;
					String recipient_number = splittedString[2];

					CustomizedTransferSummarySettings ctss = new CustomizedTransferSummarySettings();
					ctss.setAmountInput(amount);
					ctss.setAmountClickable(true);
					if(!amount.equals("0")) {
						ctss.setAmountClickable(false);
						goToNextStage();
					}
					ctss.setRecipientInput(recipient_number);
					ctss.setRecipientClickable(false);
					ctss.setActionRadioClickable(false);
					ctssLiveData.postValue(ctss);

					//SET PARAMETERS IN SUMMARY CARD
					setAmount(amount);
					setRecipient(recipient_number);

					new Thread(()->{
						Channel channel = repo.getChannel(channel_id);
						activeActionName.postValue(channel.name);
					}).start();

				}

				@Override
				public void onError(Exception exception) {
				}
			});

		}catch (Exception e) {
		}

	}

	boolean isDone() { return stage.getValue() == REVIEW || stage.getValue() == REVIEW_DIRECT; }

	public void view(Schedule s) {
		schedule.setValue(s);
		setType(s.type);
		setActiveChannel(s.channel_id);
		setAmount(s.amount);
		setRecipient(s.recipient);
		setNote(s.note);
		setStage(TransferStage.REVIEW_DIRECT);
	}

	public void schedule() {
		Schedule s = new Schedule(activeAction.getValue(), futureDate.getValue(), repeatSaved.getValue(), frequency.getValue(), endDate.getValue(),
			recipient.getValue(), amount.getValue(), note.getValue(), getApplication());
		saveSchedule(s);
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
}