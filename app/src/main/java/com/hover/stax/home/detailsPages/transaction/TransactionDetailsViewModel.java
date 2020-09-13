package com.hover.stax.home.detailsPages.transaction;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.ArrayUtils;
import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.api.Hover;
import com.hover.sdk.sims.SimInfo;
import com.hover.sdk.sms.MessageLog;
import com.hover.sdk.transactions.Transaction;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.home.StaxTransaction;
import com.hover.stax.utils.Utils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class TransactionDetailsViewModel extends AndroidViewModel {

	private final String TAG = "TDViewModel";
	private MutableLiveData<ArrayList<TransactionDetailsMessagesModel>> messagesModel;
	private MutableLiveData<StaxTransaction> staxTransactionMutableLiveData;

	public TransactionDetailsViewModel(@NonNull Application application) {
		super(application);
		messagesModel = new MutableLiveData<>();
		messagesModel.setValue(null);
		staxTransactionMutableLiveData = new MutableLiveData<>();


	}

	LiveData<ArrayList<TransactionDetailsMessagesModel>> loadMessagesModelObs() {
		return messagesModel;
	}

	LiveData<StaxTransaction> loadStaxTransaction() {
		return staxTransactionMutableLiveData;
	}

	void getMessagesModels(String transactionId) {
		messagesModel.postValue(getMessagesOfTransactionById(transactionId));
	}


	private Transaction getTransactionByTransId(String transId) {
		return Hover.getTransaction(transId, ApplicationInstance.getContext());
	}

	private MessageLog getSMSMessageByUUID(String uuid) {
		return Hover.getSMSMessageByUUID(uuid, ApplicationInstance.getContext());
	}

	private HoverAction getSingleActionByIdActionId(String actionId) {
		return Hover.getAction(actionId, ApplicationInstance.getContext());
	}


	private void setupStaxTransaction(Transaction transaction) {
		try {
			String lastTime = "";
			StaxTransaction staxTransaction = new StaxTransaction(transaction, lastTime, getApplication());
			staxTransaction.setNetworkName("Not in SIM slot");
			List<SimInfo> sims = Hover.getPresentSims(ApplicationInstance.getContext());
			for (SimInfo sim : sims) {
				if (sim.getOSReportedHni().equals(transaction.networkHni)) {
					staxTransaction.setNetworkName(sim.getNetworkOperatorName());
				}
			}
			staxTransactionMutableLiveData.setValue(staxTransaction);

		} catch (JSONException e) {
			Log.e(TAG, "Error parsing transaction", e);
		}
	}

	private ArrayList<TransactionDetailsMessagesModel> getMessagesOfTransactionById(String transactionId) {
		String[][] result = getTransactionMessagesByIdFromHover(transactionId);
		String[] enteredValues = result[0];
		String[] ussdMessages = result[1];
		int largestSize = Math.max(enteredValues.length, ussdMessages.length);
		ArrayList<TransactionDetailsMessagesModel> messagesModels = new ArrayList<>();

		//Put in a try and catch to prevent crashing when USSD session reports incorrectly.
		try {
			for (int i = 0; i < largestSize; i++) {
				TransactionDetailsMessagesModel model = new TransactionDetailsMessagesModel(
						enteredValues[i] != null ? enteredValues[i] : "",
						ussdMessages[i] != null ? ussdMessages[i] : "");
				if (!messagesModels.contains(model)) messagesModels.add(model);
			}
		} catch (Exception e) {

			//PUTTING IN ANOTHER TRY AND CATCH TO AVOID ERROR WHEN ON NO-SIM MODE
			try {
				for (int i = 0; i < largestSize - 1; i++) {
					TransactionDetailsMessagesModel model = new TransactionDetailsMessagesModel(
							enteredValues[i] != null ? enteredValues[i] : "",
							ussdMessages[i] != null ? ussdMessages[i] : "");

				}
			} catch (Exception ex) {
				//USE THIS FOR NO-SIM MESSAGE MODE;
				messagesModels.add(new TransactionDetailsMessagesModel("*ROOT_CODE#", "Test Responses"));
			}


		}

		return messagesModels;
	}

	private String[][] getTransactionMessagesByIdFromHover(String transactionId) {
		Transaction transaction = getTransactionByTransId(transactionId);
		setupStaxTransaction(transaction);

		HoverAction action = getSingleActionByIdActionId(transaction.actionId);
		List<MessageLog> smsMessages = new ArrayList<>();
		try {
			String[] smsUUIDS = Utils.convertNormalJSONArrayToStringArray(transaction.smsHits);
			for (String uuid : smsUUIDS) {
				MessageLog log = getSMSMessageByUUID(uuid);
				smsMessages.add(log);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}


		String[] smsSenderList = new String[smsMessages.size()];
		String[] smsContentList = new String[smsMessages.size()];
		for (int i = 0; i < smsMessages.size(); i++) {
			smsSenderList[i] = "";
			smsContentList[i] = smsMessages.get(i).msg;
		}
		String[] rootCode = {action.rootCode};
		String[] tempEnteredValues = {};
		try {
			tempEnteredValues = Utils.convertNormalJSONArrayToStringArray(transaction.enteredValues);
		} catch (JSONException ignored) {
		}

		int aLen = rootCode.length;
		int bLen = tempEnteredValues.length;
		String[] enteredValues = new String[aLen + bLen];
		System.arraycopy(rootCode, 0, enteredValues, 0, aLen);
		System.arraycopy(tempEnteredValues, 0, enteredValues, aLen, bLen);
		enteredValues = ArrayUtils.concat(enteredValues, smsSenderList);

		String[] ussdMessages = {};
		try {
			ussdMessages = Utils.convertNormalJSONArrayToStringArray(transaction.ussdMessages);
			ussdMessages = ArrayUtils.concat(ussdMessages, smsContentList);
		} catch (Exception ignored) {
		}
		return new String[][]{enteredValues, ussdMessages};
	}
}
