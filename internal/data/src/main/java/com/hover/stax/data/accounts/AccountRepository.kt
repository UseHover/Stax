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

import android.content.Context
import androidx.lifecycle.LiveData
import com.hover.sdk.api.ActionApi
import com.hover.sdk.sims.SimInfo
import com.hover.stax.data.R
import com.hover.stax.data.actions.ActionRepo
import com.hover.stax.data.channel.ChannelRepository
import com.hover.stax.database.dao.AccountDao
import com.hover.stax.database.models.Account
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject
import javax.inject.Inject

interface AccountRepository {

    fun getAllAccounts(): List<Account>

    fun getAllLiveAccounts(): LiveData<List<Account>>

    fun getTelecomAccounts(simSubscriptionIds: IntArray): Flow<List<Account>>

    fun getAccountsCount(): Int

    fun getAccountsByChannel(channelId: Int): List<Account>

    fun getDefaultAccount(): Account?

    fun getAccount(id: Int): Account?

    fun getLiveAccount(id: Int?): LiveData<Account>

    suspend fun saveAccount(account: Account)

    fun insert(account: Account)

    suspend fun insert(accounts: List<Account>): List<Long>

    suspend fun update(account: Account?): Int?

    suspend fun update(accounts: List<Account>): Int

    suspend fun delete(account: Account)

    val addedAccounts: Flow<List<Account>>

    suspend fun createAccount(sim: SimInfo): Account

    suspend fun getAccountBySim(subscriptionId: Int): Account?
}

class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val channelRepository: ChannelRepository,
    val actionRepo: ActionRepo,
    @ApplicationContext val context: Context
) : AccountRepository {

    // TODO -FIX ME
//    AccountRepository, PushNotificationTopicsInterface {

    override fun getAllAccounts(): List<Account> = accountDao.getAllAccounts()

    override fun getAllLiveAccounts(): LiveData<List<Account>> = accountDao.getLiveAccounts()

    override fun getTelecomAccounts(simSubscriptionIds: IntArray): Flow<List<Account>> =
        accountDao.getAccountsBySubscribedSim(simSubscriptionIds)

    override fun getAccountsCount(): Int = accountDao.getDataCount()

    override fun getAccountsByChannel(channelId: Int): List<Account> =
        accountDao.getAccountsByChannel(channelId)

    override fun getDefaultAccount(): Account? = accountDao.getDefaultAccount()

    override fun getAccount(id: Int): Account? = accountDao.getAccount(id)

    override fun getLiveAccount(id: Int?): LiveData<Account> = accountDao.getLiveAccount(id)

    override suspend fun saveAccount(account: Account) {
        if (account.id == 0) {
            accountDao.insert(account)
        } else {
            accountDao.update(account)
        }
    }

    override fun insert(account: Account) = accountDao.insert(account)

    override suspend fun insert(accounts: List<Account>): List<Long> =
        accountDao.insertAll(accounts)

    override suspend fun update(account: Account?): Int? = account?.let { accountDao.update(it) }

    override suspend fun update(accounts: List<Account>): Int = accountDao.update(accounts)

    override suspend fun delete(account: Account) = accountDao.delete(account)

    override val addedAccounts: Flow<List<Account>>
        get() = accountDao.getAccounts()

    override suspend fun createAccount(sim: SimInfo): Account {
        var account = Account(generateSimBasedName(sim), generateSimBasedAlias(sim))
        channelRepository.getTelecom(sim.osReportedHni)?.let {
            account = Account(account.userAlias, it, false, sim.subscriptionId)
            accountDao.insert(account)
        }
        logChoice(account)
        ActionApi.scheduleActionConfigUpdate(account.countryAlpha2, 24, context)
        return account
    }

    private fun generateSimBasedName(sim: SimInfo): String {
        return (
                sim.operatorName ?: sim.networkOperatorName
                ?: ""
                ) + "-" + sim.subscriptionId.toString()
    }

    private fun generateSimBasedAlias(sim: SimInfo): String {
        return sim.operatorName ?: sim.networkOperatorName ?: "Unknown"
    }

    override suspend fun getAccountBySim(simSubscriptionId: Int): Account? =
        accountDao.getAccountBySim(simSubscriptionId)

    // TODO -FIX ME
    private fun logChoice(account: Account) {
//        joinChannelGroup(account.channelId, context)
        val args = JSONObject()

        try {
            args.put(context.getString(R.string.added_channel_id), account.channelId)
        } catch (ignored: Exception) {
        }

//        AnalyticsUtil.logAnalyticsEvent(context.getString(R.string.new_sim_channel), args, context)
    }
}