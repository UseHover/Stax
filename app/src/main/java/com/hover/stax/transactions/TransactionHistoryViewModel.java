package com.hover.stax.transactions;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.database.DatabaseRepo;

import java.util.List;

public class TransactionHistoryViewModel extends AndroidViewModel {
	private final String TAG = "TransactionHistoryViewModel";

	private DatabaseRepo repo;

	private LiveData<List<StaxTransaction>> transactions;

	public TransactionHistoryViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);

		transactions = new MutableLiveData<>();
		transactions = repo.getCompleteTransferTransactions();
	}

	public LiveData<List<StaxTransaction>> getStaxTransactions() { return transactions; }

	public void saveTransaction(Intent data, Context c) {
		new Thread(() -> {
			StaxTransaction t = new StaxTransaction(data, repo.getAction(data.getStringExtra(TransactionContract.COLUMN_ACTION_ID)), c);
			if (t.uuid != null) { repo.insert(t); }
		}).start();
	}
}
