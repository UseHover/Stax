package com.hover.stax.accounts

import android.app.Application
import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.stax.actions.ActionRepo
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelRepo
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.domain.model.Account
import com.hover.stax.transactions.StaxTransaction
import com.hover.stax.transactions.TransactionHistory
import com.hover.stax.transactions.TransactionRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class AccountDetailViewModel(val application: Application, val repo: AccountRepo, private val transactionRepo: TransactionRepo,
                             private val channelRepo: ChannelRepo, val actionRepo: ActionRepo) : ViewModel() {

    private val id = MutableLiveData<Int>()
    var account: LiveData<Account> = MutableLiveData()
    var channel: LiveData<Channel> = MutableLiveData()
    private var transactions: LiveData<List<StaxTransaction>> = MutableLiveData()
    var transactionHistory : MediatorLiveData<List<TransactionHistory>> = MediatorLiveData()
    var actions: LiveData<List<HoverAction>> = MutableLiveData()
    var spentThisMonth: LiveData<Double> = MutableLiveData()
    var feesThisYear: LiveData<Double> = MutableLiveData()

    private val calendar = Calendar.getInstance()

    init {
        account = Transformations.switchMap(id, repo::getLiveAccount)
        channel = Transformations.switchMap(account) { it?.let { channelRepo.getLiveChannel(it.channelId) } }
        transactions = Transformations.switchMap(account) { it?.let { transactionRepo.getAccountTransactions(it) } }
        actions = Transformations.switchMap(id, actionRepo::getChannelActions)
        spentThisMonth = Transformations.switchMap(id, this::loadSpentThisMonth)
        feesThisYear = Transformations.switchMap(id, this::loadFeesThisYear)
        transactionHistory.addSource(transactions, this::getTransactionHistory)
    }

    fun setAccount(accountId: Int) = id.postValue(accountId)

    private fun loadSpentThisMonth(id: Int): LiveData<Double>? =
        transactionRepo.getSpentAmount(id, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))

    private fun loadFeesThisYear(id: Int): LiveData<Double>? = transactionRepo.getFees(id, calendar.get(Calendar.YEAR))

    fun updateAccountName(newName: String) = viewModelScope.launch {
        val a = account.value!!
        a.alias = newName
        repo.update(a)
    }

    fun updateAccountNumber(newNumber: String) = viewModelScope.launch {
        val a = account.value!!
        a.accountNo = newNumber
        repo.update(a)
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

    fun removeAccount(account: Account) = viewModelScope.launch(Dispatchers.IO) {
        val channelsToUpdate = mutableSetOf<Channel>()

        if (repo.getAccountsByChannel(account.channelId).size == 1) {
            val channel = channelRepo.getChannel(account.channelId)!!
            channel.selected = false
            channelsToUpdate.add(channel)
        }

        repo.delete(account)
        transactionRepo.deleteAccountTransactions(account.id)

        val accounts = repo.getAllAccounts()
        val changeDefault = account.isDefault

        if (accounts.isNotEmpty() && changeDefault)
            accounts.firstOrNull()?.let {
                it.isDefault = true
                repo.update(it)

                val channel = channelRepo.getChannel(it.channelId)!!
                channel.selected = true
                channelsToUpdate.add(channel)
            }

        channelRepo.update(channelsToUpdate.toList())

    }
}