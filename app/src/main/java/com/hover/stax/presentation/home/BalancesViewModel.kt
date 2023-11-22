/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.presentation.home

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.domain.model.Account
import com.hover.stax.utils.AnalyticsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class BalancesViewModel(
    application: Application,
    val actionRepo: ActionRepo,
    val accountRepo: AccountRepo
) : AndroidViewModel(application) {

    var userRequestedBalanceAccount = MutableLiveData<Account?>()

    private var _balanceAction = MutableSharedFlow<HoverAction>()
    val balanceAction = _balanceAction.asSharedFlow()

    private val _accounts = MutableLiveData<List<Account>>()
    val accounts: LiveData<List<Account>> = _accounts

    private val _actionRunError = Channel<String>()
    val actionRunError = _actionRunError.receiveAsFlow()

    init {
        getAccounts()
    }

    fun requestAction(account: Account?, type: String) {
        if (account == null) {
            AnalyticsUtil.logAnalyticsEvent(
                (getApplication() as Context).getString(R.string.action_failed, type),
                getApplication()
            )
            Toast.makeText(getApplication(), (getApplication() as Context).getString(R.string.action_failed, type), Toast.LENGTH_LONG).show()
        } else {
            AnalyticsUtil.logAnalyticsEvent(
                (getApplication() as Context).getString(R.string.action_starting, type),
                getApplication()
            )
            userRequestedBalanceAccount.value = account
            startActionFor(userRequestedBalanceAccount.value, type)
        }
    }

    private fun startActionFor(account: Account?, type: String) = viewModelScope.launch(Dispatchers.IO) {
        if (account == null) return@launch

        val action = actionRepo.getFirstAction(account.institutionId, account.countryAlpha2, type)
        action?.let { _balanceAction.emit(action) } ?: run { _actionRunError.send((getApplication() as Context).getString(R.string.error_running_action)) }
    }

    private fun getAccounts() = viewModelScope.launch {
        accountRepo.getAccounts().collect { _accounts.postValue(it) }
    }
}