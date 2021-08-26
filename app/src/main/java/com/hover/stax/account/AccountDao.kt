package com.hover.stax.account

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts ORDER BY alias ASC")
    fun getAllAccounts(): LiveData<List<Account>>

    @Query("SELECT * FROM accounts WHERE channelId = :channelId ORDER BY alias ASC")
    fun getAccounts(channelId: Int): List<Account>

    @Query("SELECT * FROM accounts WHERE channelId in (:channelIds) ORDER BY alias ASC")
    suspend fun getAccounts(channelIds: List<Int>): List<Account>

    @Query("SELECT * FROM accounts where name = :name and channelId = :channelId")
    fun getAccount(name: String, channelId: Int): Account?

    @Query("SELECT COUNT(id)  FROM accounts")
    fun getDataCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg accounts: Account)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(account: Account)

    @Update
    fun update(account: Account)

    @Update
    fun updateAll(accounts: List<Account>)

    @Delete
    fun delete(account: Account)

    @Query("DELETE FROM accounts")
    fun deleteAll()
}