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

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.data.accounts.AccountRepository
import com.hover.stax.data.actions.ActionRepo
import com.hover.stax.database.models.Account
import com.hover.stax.utils.AnalyticsUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BalancesViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val actionRepo: ActionRepo,
    private val accountRepo: AccountRepository
) : ViewModel() {

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

    fun requestBalance(account: Account?) {
        if (account == null) {
            AnalyticsUtil.logAnalyticsEvent(
                context.getString(R.string.refresh_balance_failed),
                context
            )
            Toast.makeText(context, R.string.refresh_balance_failed, Toast.LENGTH_LONG).show()
        } else {
            AnalyticsUtil.logAnalyticsEvent(
                (context as Context).getString(R.string.refresh_balance),
                context
            )
            userRequestedBalanceAccount.value = account
            startBalanceActionFor(userRequestedBalanceAccount.value)
        }
    }

    private fun startBalanceActionFor(account: Account?) = viewModelScope.launch(Dispatchers.IO) {
        if (account == null) return@launch

        val action = actionRepo.getFirstAction(
            account.institutionId,
            account.countryAlpha2,
            HoverAction.BALANCE
        )
        action?.let { _balanceAction.emit(action) } ?: run {
            _actionRunError.send(
                context.getString(
                    R.string.error_running_action
                )
            )
        }
    }

    private fun getAccounts() = viewModelScope.launch {
        accountRepo.addedAccounts.collect { _accounts.postValue(it) }
    }
}