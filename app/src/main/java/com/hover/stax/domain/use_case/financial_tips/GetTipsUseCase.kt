package com.hover.stax.domain.use_case.financial_tips

import com.hover.stax.domain.model.FinancialTip
import com.hover.stax.domain.repository.FinancialTipsRepository
import kotlinx.coroutines.flow.Flow

class GetTipsUseCase(financialTipsRepository: FinancialTipsRepository) {

    val tips : Flow<List<FinancialTip>> = financialTipsRepository.tips

}