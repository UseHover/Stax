package com.hover.stax.domain.use_case.bonus

import com.hover.stax.domain.model.Bonus
import com.hover.stax.domain.repository.BonusRepository
import kotlinx.coroutines.flow.Flow

class GetBonusesUseCase(private val repository: BonusRepository) {

    suspend operator fun invoke(): Flow<List<Bonus>> = repository.getBonusList()

}