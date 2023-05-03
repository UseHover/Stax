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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.domain.model.Account
import com.hover.stax.database.models.Channel
import com.hover.stax.database.channel.repository.ChannelRepository
import com.hover.stax.transactions.StaxTransaction
import com.hover.stax.transactions.TransactionHistoryItem
import com.hover.stax.transactions.TransactionRepo
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountDetailViewModel(
    val application: Application,
    val repo: AccountRepo,
    private val transactionRepo: TransactionRepo,
    private val channelRepository: ChannelRepository,
    val actionRepo: ActionRepo
) : ViewModel() {

    private val id = MutableLiveData<Int>()
    var account: LiveData<Account> = MutableLiveData()
    var channel: LiveData<Channel> = MutableLiveData()
    private var transactions: LiveData<List<StaxTransaction>> = MutableLiveData()
    var transactionHistoryItem: MediatorLiveData<List<TransactionHistoryItem>> = MediatorLiveData()
    var spentThisMonth: LiveData<Double> = MutableLiveData()
    var feesThisYear: LiveData<Double> = MutableLiveData()

    private val calendar = Calendar.getInstance()

    init {
        account = Transformations.switchMap(id, repo::getLiveAccount)
        channel = Transformations.switchMap(account) { it?.let { channelRepository.getLiveChannel(it.channelId) } }
        transactions = Transformations.switchMap(account) { it?.let { transactionRepo.getAccountTransactions(it) } }
        spentThisMonth = Transformations.switchMap(id, this::loadSpentThisMonth)
        feesThisYear = Transformations.switchMap(id, this::loadFeesThisYear)
        transactionHistoryItem.addSource(transactions, this::getTransactionHistory)
    }

    fun setAccount(accountId: Int) = id.postValue(accountId)

    private fun loadSpentThisMonth(id: Int): LiveData<Double>? =
        transactionRepo.getSpentAmount(id, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))

    private fun loadFeesThisYear(id: Int): LiveData<Double>? = transactionRepo.getFees(id, calendar.get(Calendar.YEAR))

    fun updateAccountName(newName: String) = viewModelScope.launch(Dispatchers.IO) {
        val a = account.value!!
        a.userAlias = newName
        repo.update(a)
    }

    fun updateAccountNumber(newNumber: String) = viewModelScope.launch(Dispatchers.IO) {
        val a = account.value!!
        a.accountNo = newNumber
        repo.update(a)
    }

    private fun getTransactionHistory(transactions: List<StaxTransaction>) {
        viewModelScope.launch(Dispatchers.IO) {
            val history = transactions.asSequence().map {
                val action = actionRepo.getAction(it.action_id)
                var institutionName = ""
                action?.let {
                    institutionName = action.from_institution_name
                }
                TransactionHistoryItem(it, action, institutionName)
            }.toList()
            transactionHistoryItem.postValue(history)
        }
    }

    fun removeAccount(account: Account) = viewModelScope.launch(Dispatchers.IO) {
        repo.delete(account)
        transactionRepo.deleteAccountTransactions(account.id)

        val accounts = repo.getAllAccounts()
        val changeDefault = account.isDefault

        if (accounts.isNotEmpty() && changeDefault)
            accounts.firstOrNull()?.let {
                it.isDefault = true
                repo.update(it)
            }
    }
}