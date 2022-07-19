package com.hover.stax.domain.use_case.financial_tips

import com.hover.stax.domain.model.FinancialTip
import com.hover.stax.domain.model.Resource
import com.hover.stax.domain.repository.FinancialTipsRepository
import kotlinx.coroutines.flow.*

class GetTipsUseCase(private val financialTipsRepository: FinancialTipsRepository) {

    operator fun invoke(): Flow<Resource<List<FinancialTip>>> = flow {
        try {
            emit(Resource.Loading())

            val financialTips = financialTipsRepository.getTips()
            emit(Resource.Success(financialTips))
        } catch (e: Exception) {
            emit(Resource.Error("Error fetching tips"))
        }
    }

}