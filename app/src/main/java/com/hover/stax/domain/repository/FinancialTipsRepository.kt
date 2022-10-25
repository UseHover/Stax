package com.hover.stax.domain.repository

import com.hover.stax.domain.model.FinancialTip
import kotlinx.coroutines.flow.Flow

interface FinancialTipsRepository {

    suspend fun getTips(): List<FinancialTip>

    fun getDismissedTipId() : String?

    fun dismissTip(id: String)
}