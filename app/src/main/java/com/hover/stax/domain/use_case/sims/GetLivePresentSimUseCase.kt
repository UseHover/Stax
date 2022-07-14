package com.hover.stax.domain.use_case.sims
import androidx.lifecycle.LiveData
import com.hover.sdk.sims.SimInfo
import com.hover.stax.domain.repository.SimRepository

class GetLivePresentSimUseCase(private val simRepository: SimRepository) {
	operator fun invoke() : LiveData<List<SimInfo>> {
		return simRepository.getPresentSimsLive()
	}
}