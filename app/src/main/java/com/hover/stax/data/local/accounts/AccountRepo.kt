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
package com.hover.stax.data.local.accounts

import androidx.lifecycle.LiveData
import com.hover.stax.database.AppDatabase
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.model.USDCAccount
import com.hover.stax.domain.model.USSDAccount
import kotlinx.coroutines.flow.Flow

class AccountRepo(db: AppDatabase) {

    private val USSDAccountDao: USSDAccountDao = db.accountDao()

    fun getAllAccounts(): List<USSDAccount> = USSDAccountDao.getAllAccounts()

    fun getAllLiveAccounts(): LiveData<List<USSDAccount>> = USSDAccountDao.getLiveAccounts()

    fun getAccountBySim(simSubscriptionId: Int): USSDAccount? =
        USSDAccountDao.getAccountBySim(simSubscriptionId)

    fun getTelecomAccounts(simSubscriptionIds: IntArray): Flow<List<Account>> =
        USSDAccountDao.getAccountsBySubscribedSim(simSubscriptionIds)

    fun getAccountsCount(): Int = USSDAccountDao.getDataCount()

    fun getAccountsByChannel(channelId: Int): List<Account> =
        USSDAccountDao.getAccountsByChannel(channelId)

    fun getDefaultAccount(): Account? = USSDAccountDao.getDefaultAccount()

    fun getAccount(id: Int): USSDAccount? = USSDAccountDao.getAccount(id)

    fun getLiveAccount(id: Int?): LiveData<USSDAccount> = USSDAccountDao.getLiveAccount(id)

    fun getAccounts(): Flow<List<USSDAccount>> = USSDAccountDao.getAccounts()

    suspend fun saveAccount(account: Account) {
        if (account.id == 0) {
            USSDAccountDao.insert(account)
        } else {
            USSDAccountDao.update(account)
        }
    }

    fun insert(account: Account) = USSDAccountDao.insert(account)

    fun insert(account: USSDAccount) = USSDAccountDao.insert(account)

    fun insert(account: USDCAccount) = USSDAccountDao.insert(account)

    suspend fun insert(accounts: List<Account>): List<Long> = USSDAccountDao.insertAll(accounts)

    suspend fun update(account: Account?) = account?.let { USSDAccountDao.update(it) }

    suspend fun update(accounts: List<Account>) = USSDAccountDao.update(accounts)

    suspend fun delete(account: Account) = USSDAccountDao.delete(account)
}