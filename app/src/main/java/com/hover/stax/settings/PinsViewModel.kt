package com.hover.stax.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.account.Account
import com.hover.stax.channels.Channel
import com.hover.stax.database.DatabaseRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class PinsViewModel(val repo: DatabaseRepo) : ViewModel() {

    var accounts: LiveData<List<Account>> = MutableLiveData()
    var account = MutableLiveData<Account>()
    val channel = MutableLiveData<Channel>()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        accounts = repo.allAccounts
    }

    fun loadAccount(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val a = repo.getAccount(id)
            account.postValue(a)
            channel.postValue(repo.getChannel(a!!.channelId))
        }
    }

    fun savePin(channel: Channel, c: Context) {
        if (channel.pin != null && channel.pin.isNotEmpty()) {
            channel.pin = KeyStoreExecutor.createNewKey(channel.pin, c)
            repo.update(channel)
        }
    }

    fun removeAccount(account: Account) = viewModelScope.launch(Dispatchers.IO) {
        val defaultChanged = account.isDefault

        repo.delete(account)
        val hasAccounts = repo.getAccounts(account.channelId).isNotEmpty()
        if (!hasAccounts) {
            val channel = repo.getChannel(account.channelId).apply {
                selected = false
                defaultAccount = false
            }
            Timber.e("removing channel from selected")
            repo.update(channel)
        }

        if (!accounts.value.isNullOrEmpty() && defaultChanged)
            accounts.value?.firstOrNull()?.let {
                it.isDefault = true
                repo.update(it)
            }
    }

    fun setDefaultAccount(account: Account) {
        if (!accounts.value.isNullOrEmpty()) {
            for (a in accounts.value!!) {
                a.isDefault = a.id == account.id
                repo.update(a)
            }
        }
    }

}