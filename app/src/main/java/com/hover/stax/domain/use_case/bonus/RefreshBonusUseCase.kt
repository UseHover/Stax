package com.hover.stax.domain.use_case.bonus

import com.hover.stax.domain.repository.BonusRepository

class RefreshBonusUseCase(private val repository: BonusRepository) {

    suspend operator fun invoke() = repository.refreshBonuses()

}