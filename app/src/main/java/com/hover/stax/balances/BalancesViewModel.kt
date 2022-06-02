package com.hover.stax.balances

import android.app.Application
import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.PLACEHOLDER
import com.hover.stax.actions.ActionRepo
import com.hover.stax.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class BalancesViewModel(application: Application, val actionRepo: ActionRepo) : AndroidViewModel(application) {

    private var _showBalances = MutableLiveData(false)
    val showBalances: LiveData<Boolean> = _showBalances

    var userRequestedBalanceAccount = MutableLiveData<Account?>()

    private var _balanceAction = MutableSharedFlow<HoverAction>()
    val balanceAction = _balanceAction.asSharedFlow()

    init {
        _showBalances.value = Utils.getBoolean(BalancesFragment.BALANCE_VISIBILITY_KEY, getApplication(), true)
    }

    fun setBalanceState(show: Boolean) = viewModelScope.launch {
        Utils.saveBoolean(BalancesFragment.BALANCE_VISIBILITY_KEY, show, getApplication())
        _showBalances.postValue(show)
    }

    fun requestBalance(account: Account) {
        userRequestedBalanceAccount.value = account
        startBalanceActionFor(userRequestedBalanceAccount.value)
    }

    private fun startBalanceActionFor(account: Account?) = viewModelScope.launch(Dispatchers.IO) {
        val channelId = account?.channelId ?: -1
        val action = actionRepo.getActions(channelId, if (account?.name == PLACEHOLDER) HoverAction.FETCH_ACCOUNTS else HoverAction.BALANCE).first()
        _balanceAction.emit(action)
    }
}