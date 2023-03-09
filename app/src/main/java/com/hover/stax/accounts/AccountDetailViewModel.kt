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
package com.hover.stax.accounts

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.channels.Channel
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.data.local.channels.ChannelRepo
import com.hover.stax.domain.model.USSDAccount
import com.hover.stax.domain.use_case.AccountDetail
import com.hover.stax.domain.use_case.ActionableAccount
import com.hover.stax.domain.use_case.GetAccountDetailsUseCase
import com.hover.stax.transactions.StaxTransaction
import com.hover.stax.transactions.TransactionHistoryItem
import com.hover.stax.transactions.TransactionRepo
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class AccountDetailViewModel(
    val application: Application,
    val repo: AccountRepo,
    private val transactionRepo: TransactionRepo,
    val actionRepo: ActionRepo,
    val accountUseCase: GetAccountDetailsUseCase
) : ViewModel() {

    private val _account = MutableStateFlow<AccountDetail?>(null)
    val account = _account.asStateFlow()

//    var spentThisMonth: MutableStateFlow<Double?>
//    = _account.map {
//        if (it != null) {
//            val calendar = Calendar.getInstance()
//            return transactionRepo.getSpentAmount(it.account.id, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
//        }
//    }
//    var feesThisYear: LiveData<Double> = MutableLiveData()

    init {
        viewModelScope.launch(Dispatchers.IO) {
//            val spentThisMonth = _account.map {
//                flow {
//                    if (it != null) {
//                        val calendar = Calendar.getInstance()
//                        spentThisMonth.emit(
//                            transactionRepo.getSpentAmount(
//                                it.account.id,
//                                calendar.get(Calendar.MONTH) + 1,
//                                calendar.get(Calendar.YEAR)
//                            )
//                        )
//                    }
//                }
//            }
//
//            val feesThisYear = _account.flatMapLatest {
//                flow {
//                    if (it != null) {
//                        val calendar = Calendar.getInstance()
//                        emit(transactionRepo.getFees(it.account.id, calendar.get(Calendar.YEAR)))
//                    }
//                }
//            }
        }
    }

    fun setAccount(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        Timber.e("Loading account $id")
        _account.update { accountUseCase(id) }
    }

    fun updateAccountName(newName: String) = viewModelScope.launch(Dispatchers.IO) {
        val a = account.value!!.ussdAccount!!
        a.userAlias = newName
        repo.update(a)
    }

    fun updateAccountNumber(newNumber: String) = viewModelScope.launch(Dispatchers.IO) {
        val a = account.value!!.ussdAccount!!
        a.accountNo = newNumber
        repo.update(a)
    }

    fun removeAccount(account: USSDAccount) = viewModelScope.launch(Dispatchers.IO) {
        repo.delete(account)
        transactionRepo.deleteAccountTransactions(account.id)

        val accounts = repo.getUssdAccounts()
        val changeDefault = account.isDefault

        if (accounts.isNotEmpty() && changeDefault)
            accounts.firstOrNull()?.let {
                it.isDefault = true
                repo.update(it)
            }
    }
}