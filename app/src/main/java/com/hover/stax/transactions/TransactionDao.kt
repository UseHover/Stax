package com.hover.stax.transactions

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TransactionDao {
    @Query("SELECT * FROM stax_transactions WHERE channel_id = :channelId AND transaction_type != 'balance' AND status != 'failed' AND environment != 3 ORDER BY initiated_at DESC")
    fun getCompleteAndPendingTransfers(channelId: Int): LiveData<List<StaxTransaction?>?>?

    @Query("SELECT * FROM stax_transactions WHERE transaction_type != 'balance' AND status != 'failed' AND environment != 3 ORDER BY initiated_at DESC")
    fun getCompleteAndPendingTransfers(): LiveData<List<StaxTransaction?>?>?

    @get:Query("SELECT * FROM stax_transactions WHERE status != 'failed' AND environment != 3 ORDER BY initiated_at DESC LIMIT 4")
    val transactionsForAppReview: LiveData<List<StaxTransaction?>?>?

    @get:Query("SELECT * FROM stax_transactions WHERE environment = 3 ORDER BY initiated_at DESC")
    val bountyTransactions: LiveData<List<StaxTransaction?>?>?

    @Query("SELECT * FROM stax_transactions WHERE uuid = :uuid LIMIT 1")
    fun getTransaction(uuid: String?): StaxTransaction?

    @Query("SELECT SUM(amount) as total FROM stax_transactions WHERE strftime('%m', initiated_at/1000, 'unixepoch') = :month AND strftime('%Y', initiated_at/1000, 'unixepoch') = :year AND channel_id = :channelId AND environment != 3")
    fun getTotalAmount(channelId: Int, month: String?, year: String?): LiveData<Double?>?

    @Query("SELECT SUM(fee) as total FROM stax_transactions WHERE strftime('%Y', initiated_at/1000, 'unixepoch') = :year AND channel_id = :channelId AND environment != 3")
    fun getTotalFees(channelId: Int, year: String?): LiveData<Double?>?

    @Query("SELECT COUNT(id) FROM stax_transactions WHERE strftime('%m', initiated_at/1000, 'unixepoch') = :month AND strftime('%Y', initiated_at/1000, 'unixepoch') = :year AND environment != 3")
    suspend fun getTransactionCount(month: String?, year: String?): Int?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(transaction: StaxTransaction?)

    @Update
    fun update(transaction: StaxTransaction?)
}