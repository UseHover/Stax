package com.hover.stax.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hover.stax.accounts.Account
import com.hover.stax.channels.Channel
import com.hover.stax.database.DatabaseRepo

class SettingsViewModel(val repo: DatabaseRepo) : ViewModel() {

    var accounts: LiveData<List<Account>> = MutableLiveData()
    var account = MutableLiveData<Account>()
    val channel = MutableLiveData<Channel>()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        accounts = repo.allAccountsLive
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