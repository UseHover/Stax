package com.hover.stax.presentation.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo

import com.hover.stax.domain.model.Account
import com.hover.stax.domain.model.PLACEHOLDER

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class BalancesViewModel(application: Application, val actionRepo: ActionRepo, val accountRepo: AccountRepo) : AndroidViewModel(application) {

    var userRequestedBalanceAccount = MutableLiveData<Account?>()

    private var _balanceAction = MutableSharedFlow<HoverAction>()
    val balanceAction = _balanceAction.asSharedFlow()

    private val _accounts = MutableLiveData<List<Account>>()
    val accounts: LiveData<List<Account>> = _accounts

    private val _actionRunError =  Channel<String>()
    val actionRunError = _actionRunError.receiveAsFlow()

    init {
        getAccounts()
    }

    fun requestBalance(account: Account) {
        userRequestedBalanceAccount.value = account
        startBalanceActionFor(userRequestedBalanceAccount.value)
    }

    private fun startBalanceActionFor(account: Account?) = viewModelScope.launch(Dispatchers.IO) {
        if(account == null) return@launch

        val channelId = account.channelId
        val action = actionRepo.getActions(channelId, if (account.name.contains(PLACEHOLDER)) HoverAction.FETCH_ACCOUNTS else HoverAction.BALANCE).firstOrNull()
        action?.let { _balanceAction.emit(action) } ?: run { _actionRunError.send((getApplication() as Context).getString(R.string.error_running_action)) }
    }

    private fun getAccounts() = viewModelScope.launch {
        accountRepo.getAccounts().collect { _accounts.postValue(it) }
    }
}