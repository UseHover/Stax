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
import com.hover.stax.R
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.domain.model.USDCAccount
import com.hover.stax.domain.model.USSD_TYPE
import com.hover.stax.domain.use_case.ActionableAccountsUseCase
import com.hover.stax.domain.use_case.ActionableAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.stellar.sdk.Server
import org.stellar.sdk.responses.AccountResponse
import timber.log.Timber

class BalancesViewModel(
    application: Application,
    private val actionableAccountsUseCase: ActionableAccountsUseCase,
    val accountRepo: AccountRepo
) : AndroidViewModel(application) {

    val server = Server(application.getString(R.string.stellar_url))

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
        if (account.account.type == USSD_TYPE)
            _requestedBalance.send(account)
        else
            updateBalances(account.usdcAccount!!)
    }

    private fun updateBalances(account: USDCAccount) = viewModelScope.launch(Dispatchers.IO) {
        val updateBalancesResponse: AccountResponse = server.accounts().account(account.accountNo)
        val accounts = accountRepo.getUsdcAccounts()

        for (balance in updateBalancesResponse.balances) {
            accounts.find { it.accountNo == account.accountNo && it.assetCode == account.assetCode }?.also {
                it.updateBalance(balance.balance, null)
                accountRepo.update(it)
            }
        }
    }
}