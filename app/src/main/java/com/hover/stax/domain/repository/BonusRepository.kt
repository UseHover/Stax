package com.hover.stax.domain.repository

import com.hover.stax.bonus.Bonus
import kotlinx.coroutines.flow.Flow

interface BonusRepository {

    suspend fun fetchBonuses()

    suspend fun getBonusList(): Flow<List<Bonus>>

    suspend fun saveBonuses(bonusList: List<Bonus>)

    suspend fun getBonusByPurchaseChannel(channelId: Int): Bonus?

    suspend fun getBonusByUserChannel(channelId: Int): Bonus?
}