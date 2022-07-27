package com.hover.stax.presentation.financial_tips

import com.hover.stax.domain.model.FinancialTip

data class FinancialTipsState(
    val isLoading: Boolean = false,
    val tips: List<FinancialTip> = emptyList(),
    val error: String = ""
)