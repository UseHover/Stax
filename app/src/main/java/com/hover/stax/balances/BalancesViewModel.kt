package com.hover.stax.balances

import android.app.Application
import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.PLACEHOLDER
import com.hover.stax.actions.ActionRepo
import com.hover.stax.utils.Utils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class BalancesViewModel(application: Application, val actionRepo: ActionRepo) : AndroidViewModel(application) {

    private var _showBalances = MutableLiveData<Boolean>()
    val showBalances: LiveData<Boolean> = _showBalances

    var userRequestedBalanceAccount = MutableLiveData<Account?>()
    var balanceAction: LiveData<HoverAction?> = MutableLiveData()

    init {
            _showBalances.postValue(Utils.getBoolean(BalancesFragment.BALANCE_VISIBILITY_KEY, getApplication(), true))
        balanceAction = Transformations.switchMap(userRequestedBalanceAccount) { startBalanceActionFor(it) }
    }

    fun setBalanceState(show: Boolean) = viewModelScope.launch {
        Utils.saveBoolean(BalancesFragment.BALANCE_VISIBILITY_KEY, show, getApplication())
        _showBalances.postValue(show)
    }

    fun requestBalance(account: Account?) {
        userRequestedBalanceAccount.postValue(account)
    }

    private fun startBalanceActionFor(account: Account?): LiveData<HoverAction?> {
        val channelId = account?.channelId ?: -1
        return actionRepo.getFirstLiveAction(
            channelId,
            if (account?.name == PLACEHOLDER) HoverAction.BALANCE
            else HoverAction.FETCH_ACCOUNTS
        )
    }
}