package com.hover.stax.domain.use_case.sims
import com.hover.sdk.sims.SimInfo
import com.hover.stax.domain.model.Bounty
import com.hover.stax.domain.repository.BountyRepository
import com.hover.stax.domain.repository.SimRepository

class GetPresentSimUseCase(private val simRepository: SimRepository, private val bountyRepository: BountyRepository) {
	suspend operator fun invoke() : List<SimInfo> {
		return simRepository.getPresentSims()
	}

	fun simPresent(bounty: Bounty, sims: List<SimInfo>): Boolean = bountyRepository.isSimPresent(bounty, sims)
}