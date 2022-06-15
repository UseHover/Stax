package com.hover.stax.domain.use_case.bonus

import com.hover.stax.domain.repository.BonusRepository

class FetchBonusUseCase(private val repository: BonusRepository) {

    suspend operator fun invoke() {
        repository.fetchBonuses()
    }
}