package com.hover.stax.domain.use_case.financial_tips

import com.hover.stax.domain.model.FinancialTip
import com.hover.stax.domain.model.Resource
import com.hover.stax.domain.repository.FinancialTipsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TipsUseCase(private val financialTipsRepository: FinancialTipsRepository) {

    operator fun invoke(): Flow<Resource<List<FinancialTip>>> = flow {
        try {
            emit(Resource.Loading())

            val financialTips = financialTipsRepository.getTips()
            emit(Resource.Success(financialTips))
        } catch (e: Exception) {
            emit(Resource.Error("Error fetching tips"))
        }
    }

    fun getDismissedTipId(): String? = financialTipsRepository.getDismissedTipId()

    fun dismissTip(id: String) {
        financialTipsRepository.dismissTip(id)
    }
}