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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.common.base.Optional
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.model.USDCAccount
import com.hover.stax.domain.model.USSDAccount
import com.hover.stax.domain.model.USSD_TYPE
import com.hover.stax.domain.use_case.AccountBalancesUseCase
import com.hover.stax.domain.use_case.AccountWithBalance
import com.hover.stax.domain.use_case.ListSimsUseCase
import com.hover.stax.domain.use_case.SimWithAccount
import com.hover.stax.utils.AnalyticsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.stellar.sdk.Server
import org.stellar.sdk.responses.AccountResponse
import timber.log.Timber

class BalancesViewModel(
    application: Application,
    private val accountBalancesUseCase: AccountBalancesUseCase
) : AndroidViewModel(application) {

    private val _accounts = MutableStateFlow<List<AccountWithBalance>>(emptyList())
    val accounts = _accounts.asStateFlow()

    private val _userRequestedBalance = Channel<AccountWithBalance>()
    val userRequestedBalance = _userRequestedBalance.receiveAsFlow()

    private val _actionRunError = Channel<String>()
    val actionRunError = _actionRunError.receiveAsFlow()

    init {
        loadAccounts()
    }

    fun loadAccounts() = viewModelScope.launch(Dispatchers.IO) {
        Timber.e("Loading accounts")
        _accounts.update { accountBalancesUseCase() }
    }

    fun requestBalance(account: AccountWithBalance) = viewModelScope.launch(Dispatchers.IO) {
        Timber.e("requesting balance for ${account.account}")
        _userRequestedBalance.send(account)
    }
//
//    private fun requestBalance(account: USDCAccount): USDCAccount {
//        Timber.e("requesting usdc balance")
//        AnalyticsUtil.logAnalyticsEvent((getApplication() as Context).getString(R.string.refresh_usdc_balance), getApplication())
//        val server = Server("https://horizon-testnet.stellar.org")
//        val accountNo: AccountResponse = server.accounts().account(account.accountNo)
//
//        for (balance in accountNo.balances) {
//            if (balance.assetType == account.assetType && balance.assetCode == Optional.fromNullable(account.assetCode)) {
//                account.updateBalance(balance.balance, null)
//            }
//        }
//        return account
//    }
}