package com.hover.stax.domain.repository

import androidx.lifecycle.LiveData
import com.hover.sdk.sims.SimInfo
import kotlinx.coroutines.flow.Flow

interface SimRepository {
	fun getPresentSimsLive(): LiveData<List<SimInfo>>

	val presentSims: Flow<List<SimInfo>>

	suspend fun getPresentSims(): List<SimInfo>
}