package com.hover.stax.accounts

import android.app.Application
import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.transactions.StaxTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class AccountDetailViewModel(val application: Application, val repo: DatabaseRepo) : ViewModel() {

    private val id = MutableLiveData<Int>()
    var account: LiveData<Account> = MutableLiveData()
    var channel: LiveData<Channel> = MutableLiveData()
    var transactions: LiveData<List<StaxTransaction>> = MutableLiveData()
    var actions: LiveData<List<HoverAction>> = MutableLiveData()
    var spentThisMonth: LiveData<Double> = MutableLiveData()
    var feesThisYear: LiveData<Double> = MutableLiveData()

    private val calendar = Calendar.getInstance()

    init {
        account = Transformations.switchMap(id, repo::getLiveAccount)
        channel = Transformations.switchMap(account) { it?.let { repo.getLiveChannel(it.channelId) } }
        transactions = Transformations.switchMap(account, repo::getAccountTransactions)
        actions = Transformations.switchMap(id, repo::getChannelActions)
        spentThisMonth = Transformations.switchMap(id, this::loadSpentThisMonth)
        feesThisYear = Transformations.switchMap(id, this::loadFeesThisYear)
    }

    fun setAccount(accountId: Int) = id.postValue(accountId)

    private fun loadSpentThisMonth(id: Int): LiveData<Double>? =
            repo.getSpentAmount(id, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))

    private fun loadFeesThisYear(id: Int): LiveData<Double>? = repo.getFees(id, calendar.get(Calendar.YEAR))

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

    fun removeAccount(account: Account) = viewModelScope.launch(Dispatchers.IO) {
        val defaultChanged = account.isDefault
        val accounts = repo.getAllAccounts()

        if (repo.getAccounts(account.channelId).size == 1) {
            val channel = repo.getChannel(account.channelId)!!.apply {
                selected = false
                defaultAccount = false
            }
            repo.update(channel)
        }

        repo.delete(account)

        if (!accounts.isNullOrEmpty() && defaultChanged)
            accounts.firstOrNull()?.let {
                it.isDefault = true
                repo.update(it)

                val channel = repo.getChannel(it.channelId)!!.apply {
                    defaultAccount = true
                }
                repo.update(channel)
            }
    }
}