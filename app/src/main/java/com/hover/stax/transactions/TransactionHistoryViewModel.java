package com.hover.stax.transactions;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hover.sdk.actions.HoverAction;
import com.hover.stax.database.DatabaseRepo;

import java.util.List;

public class TransactionHistoryViewModel extends AndroidViewModel {

    private final DatabaseRepo repo;

    private LiveData<List<StaxTransaction>> transactions;
    private final LiveData<Boolean> appReviewLiveData;

    public TransactionHistoryViewModel(Application application) {
        super(application);
        repo = new DatabaseRepo(application);

        transactions = new MutableLiveData<>();
        transactions = repo.getCompleteAndPendingTransferTransactions();

        appReviewLiveData =  Transformations.map(repo.getTransactionsForAppReview(), this:: showAppReview);
    }

    public LiveData<List<StaxTransaction>> getStaxTransactions() { return transactions; }

    public LiveData<Boolean> showAppReviewLiveData() {
        return appReviewLiveData;
    }

    private boolean showAppReview(List<StaxTransaction> staxTransactions) {
        if(staxTransactions.size() >3) return true;

        int balancesTransactions = 0;
        int transfersAndAirtime = 0;
        for(StaxTransaction transaction : staxTransactions) {
            if(transaction.transaction_type.equals(HoverAction.BALANCE)) ++balancesTransactions;
            else ++transfersAndAirtime;
        }
        if(balancesTransactions >= 4) return true;
        return transfersAndAirtime >= 2;
    }

    public void saveTransaction(Intent data, Context c) {
        if (data != null)
            repo.insertOrUpdateTransaction(data, c);
    }
}
