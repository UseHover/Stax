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
package com.hover.stax.data.merchant

import androidx.lifecycle.LiveData
import com.hover.stax.database.dao.MerchantDao
import com.hover.stax.database.models.Merchant
import javax.inject.Inject

interface MerchantRepository {

    val all: LiveData<List<Merchant>>

    fun get(id: Int): Merchant?

    fun getMatching(tillNo: String, channelId: Int): Merchant?

    fun getLiveMatching(tillNo: String, channelId: Int): LiveData<Merchant?>

    fun save(merchant: Merchant)

    suspend fun update(merchant: Merchant)

    suspend fun delete(merchant: Merchant)
}

class MerchantRepo @Inject constructor(
    private val merchantDao: MerchantDao
) : MerchantRepository {

    override val all: LiveData<List<Merchant>> = merchantDao.all

    override fun get(id: Int): Merchant? = merchantDao.getMerchant(id)

    override fun getMatching(tillNo: String, channelId: Int): Merchant? =
        merchantDao.getMerchantsByNo(tillNo, channelId)

    override fun getLiveMatching(tillNo: String, channelId: Int): LiveData<Merchant?> =
        merchantDao.getLiveMerchantsByNo(tillNo, channelId)

    override fun save(merchant: Merchant) = merchantDao.insert(merchant)

    override suspend fun update(merchant: Merchant) = merchantDao.update(merchant)

    override suspend fun delete(merchant: Merchant) = merchantDao.delete(merchant)
}