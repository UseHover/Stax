package com.hover.stax.balances

import android.app.Application
import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.stax.accounts.Account
import com.hover.stax.actions.ActionRepo
import com.hover.stax.utils.Utils

class BalancesViewModel(application: Application, val actionRepo: ActionRepo) : AndroidViewModel(application) {

    var showBalances = MutableLiveData(true)

    var userRequestedBalanceAccount = MutableLiveData<Account?>()
    var balanceAction: LiveData<HoverAction?> = MutableLiveData()

    init {
        showBalances.value = Utils.getBoolean(BalancesFragment.BALANCE_VISIBILITY_KEY, getApplication(), true)
        balanceAction = Transformations.switchMap(userRequestedBalanceAccount) { startBalanceActionFor(it) }
    }

    fun setBalanceState(show: Boolean) {
        Utils.saveBoolean(BalancesFragment.BALANCE_VISIBILITY_KEY, show, getApplication())
        showBalances.value = show
    }

    fun requestBalance(account: Account?) {
        userRequestedBalanceAccount.postValue(account)
    }

    private fun startBalanceActionFor(account: Account?): LiveData<HoverAction?> {
        val channelId = account?.channelId ?: -1
        return actionRepo.getFirstLiveAction(channelId, HoverAction.BALANCE)
    }
}