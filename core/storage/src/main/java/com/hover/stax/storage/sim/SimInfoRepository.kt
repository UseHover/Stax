package com.hover.stax.storage.sim.repository

import com.hover.sdk.sims.SimInfo
import kotlinx.coroutines.flow.Flow

interface SimInfoRepository {
    fun getAll(): List<SimInfo>

    fun getPresentSims(): List<SimInfo>

    val flowAll: Flow<MutableList<SimInfo>>
}