package com.hover.stax.domain.use_case.bonus

import com.hover.stax.domain.model.Bonus
import com.hover.stax.domain.repository.BonusRepository
import kotlinx.coroutines.flow.Flow

class GetBonusesUseCase(repository: BonusRepository) {

    val bonusList: Flow<List<Bonus>> = repository.bonusList

}

