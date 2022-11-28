package com.hover.stax.domain.repository

import com.hover.sdk.actions.HoverAction
import com.hover.stax.domain.model.ChannelBounties
import com.hover.stax.channels.Channel
import com.hover.stax.transactions.StaxTransaction
import kotlinx.coroutines.flow.Flow

interface BountyRepository {

    val bountyActions: List<HoverAction>

    fun getCountryList(): Flow<List<String>>

    suspend fun makeBounties(actions: List<HoverAction>, transactions: List<StaxTransaction>?, channels: List<Channel>): List<ChannelBounties>
}