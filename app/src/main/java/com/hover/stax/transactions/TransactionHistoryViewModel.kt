package com.hover.stax.transactions

import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.data.local.channels.ChannelRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionHistoryViewModel(val repo: TransactionRepo, val actionRepo: ActionRepo, private val channelRepo: ChannelRepo) : ViewModel() {

    private val allNonBountyTransaction: LiveData<List<StaxTransaction>> = repo.allNonBountyTransactions
    var transactionHistoryItem: MediatorLiveData<List<TransactionHistoryItem>> = MediatorLiveData()
    private var staxTransactions: LiveData<List<StaxTransaction>> = MutableLiveData()
    private val appReviewLiveData: LiveData<Boolean>

    init {
        transactionHistoryItem.addSource(allNonBountyTransaction, this::getTransactionHistory)
        staxTransactions = repo.completeAndPendingTransferTransactions!!
        appReviewLiveData = Transformations.map(repo.transactionsForAppReview!!) { showAppReview(it) }
    }

    private fun getTransactionHistory(transactions: List<StaxTransaction>) {
        viewModelScope.launch(Dispatchers.IO) {
            val history = mutableListOf<TransactionHistoryItem>()
            for(transaction in transactions) {
                val action = actionRepo.getAction(transaction.action_id)
                var institutionName = ""
                action?.let {
                    institutionName = action.from_institution_name
                }
                history.add(TransactionHistoryItem(transaction, action, institutionName))
            }
            transactionHistoryItem.postValue(history)
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

data class TransactionHistoryItem(val staxTransaction: StaxTransaction, val action: HoverAction?, val institutionName: String)