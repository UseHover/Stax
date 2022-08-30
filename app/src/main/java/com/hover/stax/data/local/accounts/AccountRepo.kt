package com.hover.stax.data.local.accounts

import androidx.lifecycle.LiveData
import com.hover.stax.database.AppDatabase
import com.hover.stax.domain.model.Account
import com.hover.stax.utils.AnalyticsUtil
import kotlinx.coroutines.flow.Flow

class AccountRepo(db: AppDatabase) {
    private val accountDao: AccountDao = db.accountDao()

    fun getAllAccounts(): List<Account> = accountDao.getAllAccounts()

    fun getAllLiveAccounts(): LiveData<List<Account>> = accountDao.getLiveAccounts()

    fun getTelecomAccounts(simSubscriptionIds: IntArray) : Flow<List<Account>> = accountDao.getAccountsBySubscribedSim(simSubscriptionIds)

    fun getAccountsCount(): Int = accountDao.getDataCount()

    fun getAccountsByChannel(channelId: Int): List<Account> = accountDao.getAccountsByChannel(channelId)

    fun getDefaultAccount(): Account? = accountDao.getDefaultAccount()

    suspend fun getDefaultAccountAsync(): Account? = accountDao.getDefaultAccountAsync()

    fun getAccount(id: Int): Account? = accountDao.getAccount(id)

    fun getLiveAccount(id: Int?): LiveData<Account> = accountDao.getLiveAccount(id)

    fun getAccounts(): Flow<List<Account>> = accountDao.getAccounts()

    private fun getAccount(name: String, channelId: Int): Account? = accountDao.getAccount(name, channelId)

    fun saveAccounts(accounts: List<Account>) {
        accounts.forEach { account ->
            val acct = getAccount(account.name, account.channelId)

            try {
                if (acct == null) {
                    accountDao.insert(account)
                } else {
                    accountDao.update(account)
                }
            } catch (e: Exception) {
                AnalyticsUtil.logErrorAndReportToFirebase(TAG, "failed to insert/update account", e)
            }
        }
    }

    fun insert(account: Account) = accountDao.insert(account)

    suspend fun insert(accounts: List<Account>): List<Long> = accountDao.insertAll(accounts)

    fun update(account: Account?) = account?.let { accountDao.update(it) }

    suspend fun update(accounts: List<Account>) = accountDao.updateAll(accounts)

    fun delete(account: Account) = accountDao.delete(account)

    fun deleteAccount(channelId: Int, name: String) {
        accountDao.delete(channelId, name)
    }

    companion object {
        private val TAG = AccountRepo::class.java.simpleName
    }
}