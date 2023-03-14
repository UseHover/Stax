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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.domain.use_case.ActionableAccountsUseCase
import com.hover.stax.domain.use_case.ActionableAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class BalancesViewModel(
    application: Application,
    private val actionableAccountsUseCase: ActionableAccountsUseCase
) : AndroidViewModel(application) {

    private val _accounts = MutableStateFlow<List<ActionableAccount>>(emptyList())
    val accounts = _accounts.asStateFlow()

    private val _requestedBalance = Channel<ActionableAccount>()
    val requestedBalance = _requestedBalance.receiveAsFlow()

    private val _actionRunError = Channel<String>()
    val actionRunError = _actionRunError.receiveAsFlow()

    init {
        loadAccounts()
    }

    fun loadAccounts() = viewModelScope.launch(Dispatchers.IO) {
        Timber.e("Loading accounts")
        _accounts.update { actionableAccountsUseCase() }
    }

    fun requestBalance(account: ActionableAccount) = viewModelScope.launch(Dispatchers.IO) {
        Timber.e("requesting balance for ${account.account}")
        _requestedBalance.send(account)
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