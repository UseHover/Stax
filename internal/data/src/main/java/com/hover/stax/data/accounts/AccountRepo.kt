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
package com.hover.stax.data.accounts

import androidx.lifecycle.LiveData
import com.hover.stax.database.StaxDatabase
import com.hover.stax.database.dao.AccountDao
import com.hover.stax.database.models.Account
import kotlinx.coroutines.flow.Flow

class AccountRepo(db: StaxDatabase) {

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