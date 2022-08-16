package com.hover.stax.data.repository

import androidx.lifecycle.LiveData
import com.hover.sdk.sims.SimInfo
import com.hover.stax.data.local.SimRepo
import com.hover.stax.domain.repository.SimRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SimRepositoryImpl(private val simRepo: SimRepo) : SimRepository {

	override fun getPresentSimsLive(): LiveData<List<SimInfo>> {
		return simRepo.getPresentSimsLive()
	}

	override val presentSims: Flow<List<SimInfo>>
		get() = flowOf(simRepo.getPresentSims())

	override suspend fun getPresentSims(): List<SimInfo> {
		return simRepo.getPresentSims()
	}

}