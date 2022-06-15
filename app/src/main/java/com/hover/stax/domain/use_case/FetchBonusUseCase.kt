package com.hover.stax.domain.use_case

import com.hover.stax.domain.repository.BonusRepository

class FetchBonusUseCase(private val repository: BonusRepository) {

    suspend operator fun invoke() {
        repository.fetchBonuses()
    }
}