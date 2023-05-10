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
package com.hover.stax.domain.use_case.bounties

import com.hover.sdk.sims.SimInfo
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.domain.model.Bounty
import com.hover.stax.domain.model.ChannelBounties
import com.hover.stax.domain.model.Resource
import com.hover.stax.data.bounty.BountyRepository
import com.hover.stax.data.channel.ChannelRepository
import com.hover.stax.data.transactions.TransactionRepo
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetChannelBountiesUseCase(
    private val channelRepository: ChannelRepository,
    private val bountyRepository: BountyRepository,
    private val transactionRepo: TransactionRepo
) {

    fun getBounties(countryCode: String = CountryAdapter.CODE_ALL_COUNTRIES): Flow<Resource<List<ChannelBounties>>> = flow {
        try {
            emit(Resource.Loading())

            emit(Resource.Success(fetchBounties(countryCode)))
        } catch (e: Exception) {
            emit(Resource.Error("Error loading bounties"))
        }
    }

    private suspend fun fetchBounties(countryCode: String): List<ChannelBounties> {
        val channelBounties: Deferred<List<ChannelBounties>>

        coroutineScope {
            channelBounties = async(Dispatchers.IO) {
                val bountyActions = bountyRepository.bountyActions
                val bountyTransactionList = transactionRepo.bountyTransactionList
                val bountyChannels = channelRepository.filterChannels(countryCode, bountyActions)

                bountyRepository.makeBounties(bountyActions, bountyTransactionList, bountyChannels)
            }
        }

        return channelBounties.await()
    }

    fun isSimPresent(bounty: Bounty, sims: List<SimInfo>): Boolean {
        if (sims.isEmpty()) return false

        sims.forEach { simInfo ->
            for (i in 0 until bounty.action.hni_list.length()) if (bounty.action.hni_list.optString(i) == simInfo.osReportedHni) return true
        }

        return false
    }

    fun getCountryList(): Flow<List<String>> = bountyRepository.getCountryList()
}