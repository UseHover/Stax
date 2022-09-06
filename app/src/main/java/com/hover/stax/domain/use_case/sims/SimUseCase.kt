package com.hover.stax.domain.use_case.sims

import com.hover.sdk.sims.SimInfo
import com.hover.stax.data.local.SimRepo
import com.hover.stax.domain.model.Bounty
import com.hover.stax.domain.repository.BountyRepository
import com.hover.stax.domain.repository.SimRepository
import kotlinx.coroutines.flow.Flow

class SimUseCase(private val simRepo: SimRepo, private val bountyRepository: BountyRepository) {

    suspend operator fun invoke(): List<SimInfo> {
        return simRepo.getPresentSims()
    }

    val allSims: Flow<List<SimInfo>> = simRepo.all

    fun simPresent(bounty: Bounty, sims: List<SimInfo>): Boolean = bountyRepository.isSimPresent(bounty, sims)
}