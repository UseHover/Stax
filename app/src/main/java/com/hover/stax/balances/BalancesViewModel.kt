package com.hover.stax.balances

import android.app.Application
import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.AccountRepo
import com.hover.stax.actions.ActionRepo
import com.hover.stax.utils.UIHelper

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class BalancesViewModel(val application: Application, val repo: AccountRepo, val actionRepo: ActionRepo) : ViewModel() {
    var accounts = MutableLiveData<List<Account>>()
    var shouldShowBalances = MutableLiveData(false)

    private var runBalanceError = MutableLiveData<Boolean>()

    init {
        loadAccounts()
        runBalanceError.value = false
    }

    private fun loadAccounts() = viewModelScope.launch {
        repo.getAccounts().collect { accounts.postValue(it) }
    }

    fun showBalances(show: Boolean) {
        shouldShowBalances.value = show
    }
}