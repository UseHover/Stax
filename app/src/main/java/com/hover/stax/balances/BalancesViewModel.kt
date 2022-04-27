package com.hover.stax.balances

import android.app.Application
import androidx.lifecycle.*
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.AccountRepo
import com.hover.stax.actions.ActionRepo
import com.hover.stax.utils.Utils

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class BalancesViewModel(application: Application, val repo: AccountRepo, val actionRepo: ActionRepo) : AndroidViewModel(application) {

    var accounts = MutableLiveData<List<Account>>()
    var showBalances = MutableLiveData(true)

    private var runBalanceError = MutableLiveData<Boolean>()

    init {
        loadAccounts()
        runBalanceError.value = false
        showBalances.value = Utils.getBoolean(BalancesFragment.BALANCE_VISIBILITY_KEY, getApplication(), true)
    }

    private fun loadAccounts() = viewModelScope.launch {
        repo.getAccounts().collect { accounts.postValue(it) }
    }

    fun setBalanceState(show: Boolean) {
        Utils.saveBoolean(BalancesFragment.BALANCE_VISIBILITY_KEY, show, getApplication())
        showBalances.value = show
    }
}