package com.hover.stax.domain.repository

import androidx.lifecycle.LiveData
import com.hover.sdk.sims.SimInfo

interface SimRepository {
	fun getPresentSimsLive(): LiveData<List<SimInfo>>
	suspend fun getPresentSims(): List<SimInfo>
}