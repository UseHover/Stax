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
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hover.stax.storage.user.dao.BaseDao
import com.hover.stax.domain.model.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao : com.hover.stax.storage.user.dao.BaseDao<Account> {

    @Query("SELECT * FROM accounts WHERE institution_type != 'telecom' ORDER BY alias ASC")
    fun getAllAccounts(): List<Account>

    @Query("SELECT * FROM accounts WHERE institution_type != 'telecom' ORDER BY alias ASC")
    fun getLiveAccounts(): LiveData<List<Account>>

    @Query("SELECT * FROM accounts WHERE sim_subscription_id IN (:sim_subscriptionIds)")
    fun getAccountsBySubscribedSim(sim_subscriptionIds: IntArray): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE institution_type = 'telecom' AND sim_subscription_id = (:subscriptionId)")
    fun getAccountBySim(subscriptionId: Int): Account?

    @Query("SELECT * FROM accounts WHERE channelId = :channelId AND institution_type != 'telecom' ORDER BY alias ASC")
    fun getAccountsByChannel(channelId: Int): List<Account>

    @Query("SELECT * FROM accounts WHERE institutionId = :institutionId AND institution_type != 'telecom' ORDER BY alias ASC")
    fun getAccountsByInstitution(institutionId: Int): LiveData<List<Account>>

    @Query("SELECT * FROM accounts WHERE institution_type != 'telecom' ORDER BY alias ASC")
    fun getAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts where name = :name and channelId = :channelId AND institution_type != 'telecom'")
    fun getAccount(name: String, channelId: Int): Account?

    @Query("SELECT * FROM accounts where id = :id")
    fun getAccount(id: Int): Account?

    @Query("SELECT * FROM accounts where id = :id")
    fun getLiveAccount(id: Int?): LiveData<Account>

    @Query("SELECT * FROM accounts  WHERE isDefault = 1 AND institution_type != 'telecom'")
    fun getDefaultAccount(): Account?

    @Query("SELECT * FROM accounts WHERE isDefault = 1 AND institution_type != 'telecom'")
    suspend fun getDefaultAccountAsync(): Account?

    @Query("SELECT COUNT(id) FROM accounts")
    fun getDataCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(accounts: List<Account>): List<Long>

    @Query("DELETE FROM accounts")
    fun deleteAll()

    @Query("DELETE FROM accounts WHERE channelId = :channelId AND name = :name")
    fun delete(channelId: Int, name: String)
}