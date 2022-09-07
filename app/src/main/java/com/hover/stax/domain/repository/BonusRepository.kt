package com.hover.stax.domain.repository

import com.hover.stax.domain.model.Bonus
import com.hover.stax.channels.Channel
import kotlinx.coroutines.flow.Flow

interface BonusRepository {

    suspend fun refreshBonuses()

    val bonusList: Flow<List<Bonus>>

    suspend fun saveBonuses(bonusList: List<Bonus>)

    suspend fun getBonusByUserChannel(channelId: Int): Bonus?
}