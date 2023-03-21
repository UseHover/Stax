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
import androidx.room.*
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.model.USDCAccount
import com.hover.stax.domain.model.USSDAccount
import com.hover.stax.domain.model.USSD_TYPE
import com.hover.stax.storage.user.dao.BaseDao
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface AccountDao {

//    @Query("SELECT * FROM ussd_accounts")
//    fun getAllAccounts(): List<Account>

    @Transaction
    fun getAllAccounts(): List<Account> {
        val accounts = LinkedList<Account>();
        accounts.addAll(getUssdAccounts());
        accounts.addAll(getUsdcAccounts());
//        Collections.sort(mediaItems, <you can add a comparator here>);
        return accounts;
    }

    @Transaction
    fun getAccount(id: Int, type: String): Account? {
        return if (type == USSD_TYPE)
            getUssdAccount(id) as Account
        else
            getUsdcAccount(id) as Account
    }

    @Query("SELECT * FROM ussd_accounts ORDER BY alias ASC")
    fun getUssdAccounts(): List<USSDAccount>

    @Query("SELECT * FROM usdc_accounts")
    fun getUsdcAccounts(): List<USDCAccount>

    @Query("SELECT * FROM ussd_accounts ORDER BY alias ASC")
    fun getLiveAccounts(): LiveData<List<USSDAccount>>

    @Query("SELECT * FROM usdc_accounts ORDER BY alias ASC")
    fun getUSDCAccounts(): List<USDCAccount>

    @Query("SELECT * FROM ussd_accounts WHERE sim_subscription_id IN (:sim_subscriptionIds)")
    fun getAccountsBySubscribedSim(sim_subscriptionIds: IntArray): Flow<List<USSDAccount>>

    @Query("SELECT * FROM ussd_accounts WHERE institution_type = 'telecom' AND sim_subscription_id = (:subscriptionId)")
    fun getAccountBySim(subscriptionId: Int): USSDAccount?

    @Query("SELECT * FROM ussd_accounts WHERE channelId = :channelId ORDER BY alias ASC")
    fun getAccountsByChannel(channelId: Int): List<USSDAccount>

    @Query("SELECT * FROM ussd_accounts WHERE institution_type != 'telecom' ORDER BY alias ASC")
    fun getAccounts(): Flow<List<USSDAccount>>

    @Query("SELECT * FROM ussd_accounts where id = :id LIMIT 1")
    fun getUssdAccount(id: Int): USSDAccount?

    @Query("SELECT * FROM usdc_accounts where id = :id LIMIT 1")
    fun getUsdcAccount(id: Int): USDCAccount?

    @Query("SELECT * FROM ussd_accounts where id = :id")
    fun getLiveAccount(id: Int?): LiveData<USSDAccount>

    @Query("SELECT * FROM ussd_accounts  WHERE isDefault = 1 AND institution_type != 'telecom'")
    fun getDefaultAccount(): USSDAccount?

    @Query("SELECT * FROM ussd_accounts WHERE isDefault = 1 AND institution_type != 'telecom'")
    suspend fun getDefaultAccountAsync(): USSDAccount?

    @Query("SELECT COUNT(id) FROM ussd_accounts")
    fun getDataCount(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(account: USSDAccount): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(account: USDCAccount): Long

    @Update
    fun update(account: USSDAccount)

    @Update
    fun update(account: USDCAccount)

    @Delete
    fun delete(account: USSDAccount)

    @Delete
    fun delete(account: USDCAccount)

    @Query("DELETE FROM ussd_accounts WHERE channelId = :channelId AND name = :name")
    fun delete(channelId: Int, name: String)
}