package com.hover.stax.transactions

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.hover.sdk.transactions.Transaction as Txn

@Dao
interface TransactionDao {
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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(transaction: StaxTransaction?)

    @Update
    fun update(transaction: StaxTransaction?)
}