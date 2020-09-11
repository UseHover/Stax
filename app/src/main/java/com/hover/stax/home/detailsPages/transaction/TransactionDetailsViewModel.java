package com.hover.stax.home.detailsPages.transaction;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;

public class TransactionDetailsViewModel extends AndroidViewModel {

	private MutableLiveData<ArrayList<TransactionDetailsMessagesModel>> messagesModel;
	public TransactionDetailsViewModel(@NonNull Application application) {
		super(application);
		messagesModel = new MutableLiveData<>();
		messagesModel.setValue(null);
	}

	LiveData<ArrayList<TransactionDetailsMessagesModel>> loadMessagesModelObs() {return messagesModel; }
	void getMessagesModels(String transactionId) {
		messagesModel.postValue(new MessageMethods().getMessagesOfTransactionById(transactionId));
	}
}
