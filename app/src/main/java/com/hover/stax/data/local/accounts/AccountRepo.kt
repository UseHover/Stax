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

    private val AccountDao: AccountDao = db.accountDao()

    fun getAllAccounts(): List<Account> = AccountDao.getAllAccounts()

    fun getUssdAccounts(): List<USSDAccount> = AccountDao.getUssdAccounts()

    fun getAllLiveAccounts(): LiveData<List<USSDAccount>> = AccountDao.getLiveAccounts()

    fun getUsdcAccounts(): List<USDCAccount> = AccountDao.getUSDCAccounts()

    fun getAccountBySim(simSubscriptionId: Int): USSDAccount? =
        AccountDao.getAccountBySim(simSubscriptionId)

    fun getTelecomAccounts(simSubscriptionIds: IntArray): Flow<List<Account>> =
        AccountDao.getAccountsBySubscribedSim(simSubscriptionIds)

    fun getAccountsCount(): Int = AccountDao.getDataCount()

    fun getAccountsByChannel(channelId: Int): List<USSDAccount> =
        AccountDao.getAccountsByChannel(channelId)

    fun getDefaultAccount(): Account? = AccountDao.getDefaultAccount()

    suspend fun getAccount(id: Int, type: String): Account? = AccountDao.getAccount(id, type)
    suspend fun getUssdAccount(id: Int): USSDAccount? = AccountDao.getUssdAccount(id)
    suspend fun getUsdcAccount(id: Int): USDCAccount? = AccountDao.getUsdcAccount(id)

    fun getLiveAccount(id: Int?): LiveData<USSDAccount> = AccountDao.getLiveAccount(id)

    fun getAccounts(): Flow<List<USSDAccount>> = AccountDao.getAccounts()

    suspend fun saveAccount(account: USSDAccount) {
        if (account.id == 0) {
            AccountDao.insert(account)
        } else {
            AccountDao.update(account)
        }
    }

    fun insert(account: USSDAccount) = AccountDao.insert(account)
    fun insert(account: USDCAccount) = AccountDao.insert(account)

    fun update(account: USSDAccount) = AccountDao.update(account)
    fun update(account: USDCAccount) = AccountDao.update(account)

    fun delete(account: USSDAccount) = AccountDao.delete(account)
    fun delete(account: USDCAccount) = AccountDao.delete(account)
}