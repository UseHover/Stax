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

    var showBalances = MutableLiveData(true)

    init {
        showBalances.value = Utils.getBoolean(BalancesFragment.BALANCE_VISIBILITY_KEY, getApplication(), true)
    }

    fun setBalanceState(show: Boolean) {
        Utils.saveBoolean(BalancesFragment.BALANCE_VISIBILITY_KEY, show, getApplication())
        showBalances.value = show
    }
}