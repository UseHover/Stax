package com.hover.stax.accounts

import androidx.lifecycle.LiveData
import com.hover.sdk.database.HoverRoomDatabase
import com.hover.stax.database.AppDatabase
import com.hover.stax.schedules.ScheduleRepo
import com.hover.stax.utils.AnalyticsUtil
import kotlinx.coroutines.flow.Flow

class AccountRepo(db: AppDatabase) {
    private val accountDao: AccountDao = db.accountDao()

    fun getAllAccounts(): List<Account> = accountDao.getAllAccounts()

    fun getAllLiveAccounts(): LiveData<List<Account>> = accountDao.getLiveAccounts()

    fun getAccountsCount(): Int = accountDao.getDataCount()

    fun getAccountsByChannel(channelId: Int): List<Account> = accountDao.getAccountsByChannel(channelId)

    fun getDefaultAccount(): Account? = accountDao.getDefaultAccount()

    fun getAccount(id: Int): Account? = accountDao.getAccount(id)

    fun getLiveAccount(id: Int?): LiveData<Account> = accountDao.getLiveAccount(id)

    fun getAccountsByInstitution(institutionId: Int): LiveData<List<Account>> = accountDao.getAccountsByInstitution(institutionId)

    fun getAccounts(): Flow<List<Account>> = accountDao.getAccounts()

    private fun getAccount(name: String, channelId: Int): Account? = accountDao.getAccount(name, channelId)

    fun saveAccounts(accounts: List<Account>) {
        accounts.forEach { account ->
            val acct = getAccount(account.name, account.channelId)

            try {
                AppDatabase.databaseWriteExecutor.execute {
                    if (acct == null) {
                        accountDao.insert(account)
                    } else {
                        accountDao.update(account)
                    }
                }
            } catch (e: Exception) {
                AnalyticsUtil.logErrorAndReportToFirebase(TAG, "failed to insert/update account", e)
            }
        }
    }

    fun insert(account: Account) = AppDatabase.databaseWriteExecutor.execute { accountDao.insert(account) }

    fun update(account: Account?) = account?.let { AppDatabase.databaseWriteExecutor.execute { accountDao.update(it) } }

    fun delete(account: Account) = AppDatabase.databaseWriteExecutor.execute { accountDao.delete(account) }

    fun deleteAccount(channelId: Int, name: String) { accountDao.delete(channelId, name) }

    companion object {
        private val TAG = AccountRepo::class.java.simpleName
    }
}