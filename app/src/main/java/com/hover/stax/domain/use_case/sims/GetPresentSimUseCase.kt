package com.hover.stax.domain.use_case.sims
import com.hover.sdk.sims.SimInfo
import com.hover.stax.domain.repository.SimRepository

class GetPresentSimUseCase(private val simRepository: SimRepository) {
	suspend operator fun invoke() : List<SimInfo> {
		return simRepository.getPresentSims()
	}
}