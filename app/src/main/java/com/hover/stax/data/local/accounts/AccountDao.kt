package com.hover.stax.data.local.accounts

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hover.stax.domain.model.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE institution_type != 'telecom' ORDER BY alias ASC")
    fun getAllAccounts(): List<Account>

    @Query("SELECT * FROM accounts WHERE institution_type != 'telecom' ORDER BY alias ASC")
    fun getLiveAccounts(): LiveData<List<Account>>

    @Query("SELECT * FROM accounts WHERE sim_subscription_id IN (:sim_subscriptionIds)")
    fun getAccountsBySubscribedSim(sim_subscriptionIds: IntArray): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE channelId = :channelId AND institution_type != 'telecom' ORDER BY alias ASC")
    fun getAccountsByChannel(channelId: Int): List<Account>

    @Query("SELECT * FROM accounts WHERE institutionId = :institutionId AND institution_type != 'telecom' ORDER BY alias ASC")
    fun getAccountsByInstitution(institutionId: Int): LiveData<List<Account>>

    @Query("SELECT * FROM accounts WHERE institution_type != 'telecom' ORDER BY alias ASC")
    fun getAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts where name = :name and channelId = :channelId AND institution_type != 'telecom'")
    fun getAccount(name: String, channelId: Int): Account?

    @Query("SELECT * FROM accounts where id = :id")
    fun getAccount(id: Int): Account?

    @Query("SELECT * FROM accounts where name = :name")
    fun getAccount(name: String): Account?

    @Query("SELECT * FROM accounts where id = :id")
    fun getLiveAccount(id: Int?): LiveData<Account>

    @Query("SELECT * FROM accounts  WHERE isDefault = 1 AND institution_type != 'telecom'")
    fun getDefaultAccount(): Account?

    @Query("SELECT * FROM accounts WHERE isDefault = 1 AND institution_type != 'telecom'")
    suspend fun getDefaultAccountAsync(): Account?

    @Query("SELECT COUNT(id) FROM accounts")
    fun getDataCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(accounts: List<Account>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(account: Account)

    @Update
    fun update(account: Account?)

    @Update
    suspend fun updateAll(accounts: List<Account>)

    @Delete
    fun delete(account: Account)

    @Query("DELETE FROM accounts")
    fun deleteAll()

    @Query("DELETE FROM accounts WHERE channelId = :channelId AND name = :name")
    fun delete(channelId: Int, name: String)
}