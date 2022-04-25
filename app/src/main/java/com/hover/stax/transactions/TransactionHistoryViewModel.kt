package com.hover.stax.transactions

import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction

class TransactionHistoryViewModel(val repo: TransactionRepo) : ViewModel() {

    private var staxTransactions: LiveData<List<StaxTransaction>> = MutableLiveData()
    private val appReviewLiveData: LiveData<Boolean>

    fun showAppReviewLiveData(): LiveData<Boolean> {
        return appReviewLiveData
    }

    private fun showAppReview(staxTransactions: List<StaxTransaction>): Boolean {
        if (staxTransactions.size > 3) return true
        var balancesTransactions = 0
        var transfersAndAirtime = 0
        for (transaction in staxTransactions) {
            if (transaction.transaction_type == HoverAction.BALANCE) ++balancesTransactions else ++transfersAndAirtime
        }
        return if (balancesTransactions >= 4) true else transfersAndAirtime >= 2
    }

    init {
        staxTransactions = repo.completeAndPendingTransferTransactions!!
        appReviewLiveData = Transformations.map(repo.transactionsForAppReview!!) { showAppReview(it) }
    }
}