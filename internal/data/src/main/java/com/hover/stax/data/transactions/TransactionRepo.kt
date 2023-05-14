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
package com.hover.stax.data.transactions

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.transactions.Transaction
import com.hover.sdk.transactions.TransactionContract
import com.hover.stax.core.DateUtils
import com.hover.stax.database.dao.TransactionDao
import com.hover.stax.database.models.Account
import com.hover.stax.database.models.StaxContact
import com.hover.stax.database.models.StaxTransaction
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

interface TransactionRepository {

    val completeAndPendingTransferTransactions: LiveData<List<StaxTransaction>>?

    val bountyTransactions: LiveData<List<StaxTransaction>>?

    val transactionsForAppReview: LiveData<List<StaxTransaction>>?

    val allNonBountyTransactions: LiveData<List<StaxTransaction>>

    val bountyTransactionList: List<StaxTransaction>

    fun loadFromHover(uuid: String): Transaction?

    suspend fun hasTransactionLastMonth(): Boolean

    fun getAccountTransactions(account: Account): LiveData<List<StaxTransaction>>?

    fun getSpentAmount(accountId: Int, month: Int, year: Int): LiveData<Double>?

    fun getFees(accountId: Int, year: Int): LiveData<Double>?

    fun getTransactionAsync(uuid: String): Flow<StaxTransaction>

    fun deleteAccountTransactions(accountId: Int)
}

class TransactionRepo @Inject constructor(
    private val transactionDao: TransactionDao,
    private val hoverTransactionDao: com.hover.sdk.transactions.TransactionDao,
) : TransactionRepository {

    override val completeAndPendingTransferTransactions: LiveData<List<StaxTransaction>>?
        get() = transactionDao.getCompleteAndPendingTransfers()

    override val bountyTransactions: LiveData<List<StaxTransaction>>?
        get() = transactionDao.bountyTransactions

    override val transactionsForAppReview: LiveData<List<StaxTransaction>>?
        get() = transactionDao.transactionsForAppReview

    override val allNonBountyTransactions: LiveData<List<StaxTransaction>>
        get() = transactionDao.nonBountyTransactions

    override val bountyTransactionList: List<StaxTransaction>
        get() = transactionDao.bountyTransactionList

    override fun loadFromHover(uuid: String): Transaction? {
        return hoverTransactionDao.getTransactionByUUID(uuid)
    }

    @SuppressLint("DefaultLocale")
    override suspend fun hasTransactionLastMonth(): Boolean {
        return transactionDao.getTransactionCount(
            String.format(
                "%02d",
                DateUtils.lastMonth().first
            ),
            DateUtils.lastMonth().second.toString()
        )!! > 0
    }

    override fun getAccountTransactions(account: Account): LiveData<List<StaxTransaction>>? {
        return transactionDao.getAccountTransactions(account.id)
    }

    @SuppressLint("DefaultLocale")
    override fun getSpentAmount(accountId: Int, month: Int, year: Int): LiveData<Double>? {
        return transactionDao.getTotalAmount(
            accountId,
            String.format("%02d", month),
            year.toString()
        )
    }

    @SuppressLint("DefaultLocale")
    override fun getFees(accountId: Int, year: Int): LiveData<Double>? {
        return transactionDao.getTotalFees(accountId, year.toString())
    }

    private fun getTransaction(uuid: String?): StaxTransaction? {
        return transactionDao.getTransaction(uuid)
    }

    override fun getTransactionAsync(uuid: String): Flow<StaxTransaction> =
        transactionDao.getTransactionAsync(uuid)

    override fun deleteAccountTransactions(accountId: Int) =
        transactionDao.deleteAccountTransactions(accountId)

    // TODO - FIX ME
    fun insertOrUpdateTransaction(
        intent: Intent,
        action: HoverAction,
        contact: StaxContact,
        c: Context
    ) {
//        AppDatabase.databaseWriteExecutor.execute {
        try {
            var t = getTransaction(intent.getStringExtra(TransactionContract.COLUMN_UUID))
            Timber.e("Found t uuid: ${t?.uuid}")
            if (t == null) {
//                AnalyticsUtil.logAnalyticsEvent(
//                    c.getString(R.string.transaction_started),
//                    c,
//                    true
//                )
                t = StaxTransaction(intent, action, contact, c)
                transactionDao.insert(t)
                t = transactionDao.getTransaction(t.uuid)
            } else {
//                AnalyticsUtil.logAnalyticsEvent(
//                    c.getString(R.string.transaction_completed),
//                    c,
//                    true
//                )
                t.update(intent, contact)
                transactionDao.updateTransaction(t)
            }
            Timber.e("save t with uuid: %s", t?.uuid)
        } catch (e: Exception) {
//            AnalyticsUtil.logErrorAndReportToFirebase(
//                TransactionRepo::class.java.simpleName,
//                e.message,
//                e
//            )
        }
//        }
    }
}