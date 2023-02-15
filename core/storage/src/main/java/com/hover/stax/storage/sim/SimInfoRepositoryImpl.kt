package com.hover.stax.storage.sim.repository

import com.hover.sdk.sims.SimInfo
import com.hover.sdk.sims.SimInfoDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class SimInfoRepositoryImpl(
    private val simInfoDao: SimInfoDao
) : SimInfoRepository {
    override fun getAll(): List<SimInfo> = simInfoDao.all

    override fun getPresentSims(): List<SimInfo> = simInfoDao.present

    override val flowAll: Flow<MutableList<SimInfo>>
        get() = flowOf(simInfoDao.all)
}