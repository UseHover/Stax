package com.hover.stax.data.local.accounts

import androidx.lifecycle.LiveData
import com.hover.stax.database.AppDatabase
import com.hover.stax.domain.model.Account
import kotlinx.coroutines.flow.Flow

class AccountRepo(db: AppDatabase) {

    private val accountDao: AccountDao = db.accountDao()

    fun getAllAccounts(): List<Account> = accountDao.getAllAccounts()

    fun getAllLiveAccounts(): LiveData<List<Account>> = accountDao.getLiveAccounts()

    fun getAccountBySim(simSubscriptionId: Int): Account? =
        accountDao.getAccountBySim(simSubscriptionId)

    fun getTelecomAccounts(simSubscriptionIds: IntArray): Flow<List<Account>> =
        accountDao.getAccountsBySubscribedSim(simSubscriptionIds)

    fun getAccountsCount(): Int = accountDao.getDataCount()

    fun getAccountsByChannel(channelId: Int): List<Account> =
        accountDao.getAccountsByChannel(channelId)

    fun getDefaultAccount(): Account? = accountDao.getDefaultAccount()

    fun getAccount(id: Int): Account? = accountDao.getAccount(id)

    fun getLiveAccount(id: Int?): LiveData<Account> = accountDao.getLiveAccount(id)

    fun getAccounts(): Flow<List<Account>> = accountDao.getAccounts()

    suspend fun saveAccount(account: Account) {
        if (account.id == 0) {
            accountDao.insert(account)
        } else {
            accountDao.update(account)
        }
    }

    fun insert(account: Account) = accountDao.insert(account)

    suspend fun insert(accounts: List<Account>): List<Long> = accountDao.insertAll(accounts)

    suspend fun update(account: Account?) = account?.let { accountDao.update(it) }

    suspend fun update(accounts: List<Account>) = accountDao.update(accounts)

    suspend fun delete(account: Account) = accountDao.delete(account)
}