package com.hover.stax.presentation.home

import com.hover.sdk.actions.HoverAction
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.model.FinancialTip
import com.hover.stax.domain.model.Resource

data class HomeState(
    val bonuses: List<HoverAction> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val financialTips: List<FinancialTip> = emptyList(),
    val dismissedTipId: String = ""
)