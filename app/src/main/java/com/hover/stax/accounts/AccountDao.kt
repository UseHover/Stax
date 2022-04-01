package com.hover.stax.accounts

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

//    @Query("SELECT * FROM accounts ORDER BY alias ASC")
//    fun getAllAccountsLive(): LiveData<List<Account>>

    @Query("SELECT * FROM accounts ORDER BY alias ASC")
    fun getAllAccounts(): List<Account>

    @Query("SELECT * FROM accounts WHERE channelId = :channelId ORDER BY alias ASC")
    fun getAccounts(channelId: Int): List<Account>

    @Query("SELECT * FROM accounts ORDER BY alias ASC")
    fun getAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts where name = :name and channelId = :channelId")
    fun getAccount(name: String, channelId: Int): Account?

    @Query("SELECT * FROM accounts where id = :id")
    fun getAccount(id: Int): Account?

    @Query("SELECT * FROM accounts where name = :name")
    fun getAccount(name: String): Account?

    @Query("SELECT * FROM accounts where id = :id")
    fun getLiveAccount(id: Int): LiveData<Account>

    @Query("SELECT * FROM accounts where isDefault = 1")
    fun getDefaultAccount(): Account?

    @Query("SELECT COUNT(id) FROM accounts")
    fun getDataCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg accounts: Account)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(account: Account)

    @Update
    fun update(account: Account?)

    @Update
    fun updateAll(accounts: List<Account>)

    @Delete
    fun delete(account: Account)

    @Query("DELETE FROM accounts")
    fun deleteAll()

    @Query("DELETE FROM accounts WHERE channelId = :channelId AND name = :name")
    fun delete(channelId: Int, name: String)
}