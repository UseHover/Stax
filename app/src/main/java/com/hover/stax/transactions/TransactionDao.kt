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
import com.hover.sdk.transactions.Transaction as Txn
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.hover.stax.storage.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao : BaseDao<StaxTransaction> {

    @Query("SELECT * FROM stax_transactions WHERE channel_id = :channelId AND transaction_type != 'balance' AND status != 'failed' AND environment != 3 ORDER BY initiated_at DESC")
    fun getCompleteAndPendingTransfers(channelId: Int): LiveData<List<StaxTransaction>>?

    @Query("SELECT * FROM stax_transactions WHERE transaction_type != 'balance' AND status != 'failed' AND environment != 3 ORDER BY initiated_at DESC")
    fun getCompleteAndPendingTransfers(): LiveData<List<StaxTransaction>>?

    @Transaction
    @Query("SELECT * FROM stax_transactions WHERE account_id = :accountId AND environment != 3 ORDER BY initiated_at DESC")
    fun getAccountTransactions(accountId: Int): LiveData<List<StaxTransaction>>?

    @get:Query("SELECT * FROM stax_transactions WHERE status != 'failed' AND environment != 3 ORDER BY initiated_at DESC LIMIT 4")
    val transactionsForAppReview: LiveData<List<StaxTransaction>>?

    @get:Query("SELECT * FROM stax_transactions WHERE environment = 3 ORDER BY initiated_at DESC")
    val bountyTransactions: LiveData<List<StaxTransaction>>?

    @get:Query("SELECT * FROM stax_transactions WHERE environment = 3 ORDER BY initiated_at DESC")
    val bountyTransactionList: List<StaxTransaction>

    @get:Query("SELECT * FROM stax_transactions WHERE environment != 3 AND account_id IS NOT NULL ORDER BY initiated_at DESC")
    val nonBountyTransactions: LiveData<List<StaxTransaction>>

    @Query("SELECT * FROM stax_transactions WHERE uuid = :uuid LIMIT 1")
    fun getTransaction(uuid: String?): StaxTransaction?

    @Query("SELECT * FROM stax_transactions WHERE uuid = :uuid LIMIT 1")
    fun getTransactionAsync(uuid: String): Flow<StaxTransaction>

    @Query("SELECT * FROM stax_transactions WHERE uuid = :uuid LIMIT 1")
    suspend fun getTransactionSuspended(uuid: String?): StaxTransaction?

    @Query("SELECT SUM(amount) as total FROM stax_transactions WHERE strftime('%m', initiated_at/1000, 'unixepoch') = :month AND strftime('%Y', initiated_at/1000, 'unixepoch') = :year AND account_id = :accountId AND status = :status AND environment != 3")
    fun getTotalAmount(accountId: Int, month: String, year: String, status: String = Txn.SUCCEEDED): LiveData<Double>?

    @Query("SELECT SUM(fee) as total FROM stax_transactions WHERE strftime('%Y', initiated_at/1000, 'unixepoch') = :year AND account_id = :accountId AND environment != 3 AND status = :status")
    fun getTotalFees(accountId: Int, year: String, status: String = Txn.SUCCEEDED): LiveData<Double>?

    @Query("SELECT COUNT(id) FROM stax_transactions WHERE strftime('%m', initiated_at/1000, 'unixepoch') = :month AND strftime('%Y', initiated_at/1000, 'unixepoch') = :year AND environment != 3")
    suspend fun getTransactionCount(month: String, year: String): Int?

    // Need to rework the implementation on TransactionRepo
    @Update
    fun updateTransaction(transaction: StaxTransaction?)

    @Query("DELETE FROM stax_transactions WHERE account_id = :accountId")
    fun deleteAccountTransactions(accountId: Int)
}