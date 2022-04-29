package com.hover.stax.balances

import android.app.Application
import androidx.lifecycle.*
import com.hover.stax.utils.Utils

class BalancesViewModel(application: Application) : AndroidViewModel(application) {

    var showBalances = MutableLiveData(true)

    init {
        showBalances.value = Utils.getBoolean(BalancesFragment.BALANCE_VISIBILITY_KEY, getApplication(), true)
    }

    fun setBalanceState(show: Boolean) {
        Utils.saveBoolean(BalancesFragment.BALANCE_VISIBILITY_KEY, show, getApplication())
        showBalances.value = show
    }
}