package com.hover.stax.balances

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.AccountRepo
import com.hover.stax.accounts.PLACEHOLDER
import com.hover.stax.actions.ActionRepo
import com.hover.stax.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class BalancesViewModel(application: Application, val actionRepo: ActionRepo, val accountRepo: AccountRepo) : AndroidViewModel(application) {

    private var _showBalances = MutableLiveData(false)
    val showBalances: LiveData<Boolean> = _showBalances

    var userRequestedBalanceAccount = MutableLiveData<Account?>()

    private var _balanceAction = MutableSharedFlow<HoverAction>()
    val balanceAction = _balanceAction.asSharedFlow()

    private val _accounts = MutableLiveData<List<Account>>()
    val accounts: LiveData<List<Account>> = _accounts

    private val _actionRunError =  Channel<String>()
    val actionRunError = _actionRunError.receiveAsFlow()

    init {
        _showBalances.value = Utils.getBoolean(BalancesFragment.BALANCE_VISIBILITY_KEY, getApplication(), true)

        getAccounts()
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
        val action = actionRepo.getActions(channelId, if (account?.name == PLACEHOLDER) HoverAction.FETCH_ACCOUNTS else HoverAction.BALANCE).firstOrNull()
        action?.let { _balanceAction.emit(action) } ?: run { _actionRunError.send((getApplication() as Context).getString(R.string.error_running_action)) }
    }

    private fun getAccounts() = viewModelScope.launch {
        accountRepo.getAccounts().collect { _accounts.postValue(it) }
    }
}