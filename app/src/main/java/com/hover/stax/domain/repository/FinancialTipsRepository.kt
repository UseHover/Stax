package com.hover.stax.domain.repository

import com.hover.stax.domain.model.FinancialTip
import kotlinx.coroutines.flow.Flow

interface FinancialTipsRepository {

    suspend fun fetchTips(): Flow<List<FinancialTip>>
}