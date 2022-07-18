package com.hover.stax.domain.repository

import com.hover.stax.domain.model.FinancialTip
import kotlinx.coroutines.flow.Flow

interface FinancialTipsRepository {

    val tips: Flow<List<FinancialTip>>
}