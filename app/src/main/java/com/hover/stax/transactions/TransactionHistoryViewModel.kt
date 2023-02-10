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
package com.hover.stax.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.hover.sdk.actions.HoverAction
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.data.local.channels.ChannelRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionHistoryViewModel(
    val repo: TransactionRepo,
    val actionRepo: ActionRepo,
    private val channelRepo: ChannelRepo
) : ViewModel() {

    private val allNonBountyTransaction: LiveData<List<StaxTransaction>> = repo.allNonBountyTransactions
    var transactionHistoryItems: MediatorLiveData<List<TransactionHistoryItem>> = MediatorLiveData()
    private var staxTransactions: LiveData<List<StaxTransaction>> = MutableLiveData()
    private val appReviewLiveData: LiveData<Boolean>

    init {
        transactionHistoryItems.addSource(allNonBountyTransaction, this::getTransactionHistory)
        staxTransactions = repo.completeAndPendingTransferTransactions!!
        appReviewLiveData = repo.transactionsForAppReview!!.map { showAppReview(it) }
    }

    private fun getTransactionHistory(transactions: List<StaxTransaction>) {
        viewModelScope.launch(Dispatchers.IO) {
            val history = mutableListOf<TransactionHistoryItem>()
            for (transaction in transactions) {
                val action = actionRepo.getAction(transaction.action_id)
                var institutionName = ""
                action?.let {
                    institutionName = action.from_institution_name
                }
                history.add(TransactionHistoryItem(transaction, action, institutionName))
            }
            transactionHistoryItems.postValue(history)
        }
    }

    fun showAppReviewLiveData(): LiveData<Boolean> {
        return appReviewLiveData
    }

    private fun showAppReview(staxTransactions: List<StaxTransaction>): Boolean {
        if (staxTransactions.size > 3) return true
        var balancesTransactions = 0
        var transfersAndAirtime = 0
        for (transaction in staxTransactions) {
            if (transaction.transaction_type == HoverAction.BALANCE) ++balancesTransactions else ++transfersAndAirtime
        }
        return if (balancesTransactions >= 4) true else transfersAndAirtime >= 2
    }
}

data class TransactionHistoryItem(
    val staxTransaction: StaxTransaction,
    val action: HoverAction?,
    val institutionName: String
)