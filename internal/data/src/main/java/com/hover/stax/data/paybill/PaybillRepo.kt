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
package com.hover.stax.data.paybill

import com.hover.stax.database.dao.PaybillDao
import com.hover.stax.database.models.Paybill
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PaybillRepo @Inject constructor(
    private val paybillDao: PaybillDao
) {

    val allBills: Flow<List<Paybill>> = paybillDao.allBills

    fun getMatching(bizNo: String, channelId: Int): Paybill? =
        paybillDao.getPaybill(bizNo, channelId)

    fun getPaybills(accountId: Int): Flow<List<Paybill>> =
        paybillDao.getPaybillsByAccount(accountId)

    fun save(paybill: Paybill) = paybillDao.insert(paybill)

    suspend fun update(paybill: Paybill) = paybillDao.update(paybill)

    suspend fun delete(paybill: Paybill) = paybillDao.delete(paybill)
}