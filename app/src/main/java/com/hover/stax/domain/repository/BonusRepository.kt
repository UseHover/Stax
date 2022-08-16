package com.hover.stax.domain.repository

import com.hover.stax.domain.model.Bonus
import com.hover.stax.channels.Channel
import kotlinx.coroutines.flow.Flow

interface BonusRepository {

    suspend fun fetchBonuses()

    val bonusList: Flow<List<Bonus>>

    suspend fun saveBonuses(bonusList: List<Bonus>)

    suspend fun getBonusChannels(bonusList: List<Bonus>): List<Channel>

    suspend fun getBonusByPurchaseChannel(channelId: Int): Bonus?

    suspend fun getBonusByUserChannel(channelId: Int): Bonus?
}