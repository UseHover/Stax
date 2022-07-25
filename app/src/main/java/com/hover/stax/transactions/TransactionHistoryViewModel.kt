package com.hover.stax.transactions

import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.stax.actions.ActionRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionHistoryViewModel(val repo: TransactionRepo, val actionRepo: ActionRepo) : ViewModel() {

    private val allNonBountyTransaction: LiveData<List<StaxTransaction>> = repo.allNonBountyTransactions
    var transactionHistory: MediatorLiveData<List<TransactionHistory>> = MediatorLiveData()
    private var staxTransactions: LiveData<List<StaxTransaction>> = MutableLiveData()
    private val appReviewLiveData: LiveData<Boolean>

    init {
        transactionHistory.addSource(allNonBountyTransaction, this::getTransactionHistory)
        staxTransactions = repo.completeAndPendingTransferTransactions!!
        appReviewLiveData = Transformations.map(repo.transactionsForAppReview!!) { showAppReview(it) }
    }

    private fun getTransactionHistory(transactions: List<StaxTransaction>) {
        viewModelScope.launch(Dispatchers.IO) {
            val history = transactions.asSequence().map {
                val action = actionRepo.getAction(it.action_id)
                TransactionHistory(it, action)
            }.toList()
            transactionHistory.postValue(history)
        }
    }

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
}

data class TransactionHistory(val staxTransaction: StaxTransaction, val action: HoverAction?)