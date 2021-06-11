package com.hover.stax.transactions;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
        transactions = repo.getCompleteAndPendingTransferTransactions();
    }

    public LiveData<List<StaxTransaction>> getStaxTransactions() {
        return transactions;
    }
    public LiveData<List<StaxTransaction>> getStaxTransactionsForAppReview() {
        return repo.getTransactionsForAppReview();
    }

    public void saveTransaction(Intent data, Context c) {
        if (data != null)
            repo.insertOrUpdateTransaction(data, c);
    }
}
