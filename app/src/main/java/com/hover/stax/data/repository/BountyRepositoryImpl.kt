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
package com.hover.stax.data.repository

import com.hover.sdk.actions.HoverAction
import com.hover.stax.database.models.Channel
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.domain.model.Bounty
import com.hover.stax.domain.model.ChannelBounties
import com.hover.stax.domain.repository.BountyRepository
import com.hover.stax.transactions.StaxTransaction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

class BountyRepositoryImpl(
    val actionRepo: ActionRepo,
    private val coroutineDispatcher: CoroutineDispatcher
) : BountyRepository {

    override val bountyActions: List<HoverAction>
        get() = actionRepo.bounties

    override fun getCountryList(): Flow<List<String>> = channelFlow {
        launch(coroutineDispatcher) {
            val actions = bountyActions
            val countryCodes = mutableListOf(CountryAdapter.CODE_ALL_COUNTRIES)
            actions.asSequence().map { it.country_alpha2 }.distinct().sorted().toCollection(countryCodes)
            send(countryCodes)
        }
    }

    override suspend fun makeBounties(
        actions: List<HoverAction>,
        transactions: List<StaxTransaction>?,
        channels: List<Channel>
    ): List<ChannelBounties> {
        if (actions.isEmpty()) return emptyList()

        val bounties = getBounties(actions, transactions)

        return generateChannelBounties(channels, bounties)
    }

    private fun getBounties(actions: List<HoverAction>, transactions: List<StaxTransaction>?): List<Bounty> {
        val bounties: MutableList<Bounty> = ArrayList()
        val transactionList = transactions?.toMutableList() ?: mutableListOf()

        for (action in actions) {
            val filteredTransactions = transactionList.filter { it.action_id == action.public_id }
            bounties.add(Bounty(action, filteredTransactions))
        }

        return bounties
    }

    private fun generateChannelBounties(channels: List<Channel>, bounties: List<Bounty>): List<ChannelBounties> {
        if (channels.isEmpty() || bounties.isEmpty()) return emptyList()

        val openBounties = bounties.filter { it.action.bounty_is_open || it.transactionCount != 0 }

        val channelBounties = channels.filter { c ->
            openBounties.any { it.action.channel_id == c.id }
        }.map { channel ->
            ChannelBounties(channel, openBounties.filter { it.action.channel_id == channel.id })
        }

        return channelBounties
    }
}