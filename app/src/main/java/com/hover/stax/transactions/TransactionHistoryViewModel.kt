package com.hover.stax.transactions

import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.stax.channels.Channel
import com.hover.stax.contacts.StaxContact
import com.hover.stax.database.DatabaseRepo
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class TransactionHistoryViewModel(val repo: DatabaseRepo) : ViewModel() {

    private val allNonBountyTransaction : LiveData<List<StaxTransaction>> = repo.allNonBountyTransactions
    var listOfTransactionActionPair : MediatorLiveData<List<Pair<StaxTransaction, HoverAction?>>> = MediatorLiveData()
    private var staxTransactions: LiveData<List<StaxTransaction>> = MutableLiveData()
    private val appReviewLiveData: LiveData<Boolean>

    init {
        listOfTransactionActionPair.addSource(allNonBountyTransaction, this::getTransactionActionPair)
    }

    private fun getTransactionActionPair(transactions: List<StaxTransaction>) {
        viewModelScope.launch(Dispatchers.IO) {
            val listOfPair = mutableListOf<Pair<StaxTransaction, HoverAction?>>()
            transactions.forEach {
                val action= repo.getAction(it.action_id)
                val pair = Pair(it, action)
                listOfPair.add(pair)
            }
            listOfTransactionActionPair.postValue(listOfPair)
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

    suspend fun getActionAndChannel(actionId: String, channelId: Int): Pair<HoverAction, Channel> {
        val pairResult: Deferred<Pair<HoverAction, Channel>> = viewModelScope.async(Dispatchers.IO) {
            val action: HoverAction = repo.getAction(actionId)!!
            val channel: Channel = repo.getChannel(channelId)!!
            return@async Pair(action, channel)
        }

        return pairResult.await()
    }

    suspend fun getAccountNumber(contact_id: String): String? {
        val accountNumberDeferred: Deferred<String?> = viewModelScope.async {
            val contact: StaxContact? = repo.getContactAsync(contact_id)
            return@async contact?.accountNumber
        }
        return accountNumberDeferred.await()
    }

    init {
        staxTransactions = repo.completeAndPendingTransferTransactions!!
        appReviewLiveData = Transformations.map(repo.transactionsForAppReview!!) { showAppReview(it) }
    }
}