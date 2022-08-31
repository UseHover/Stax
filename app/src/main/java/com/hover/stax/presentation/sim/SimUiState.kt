package com.hover.stax.presentation.sim

import com.hover.sdk.sims.SimInfo
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.model.Bonus

data class SimUiState(
    val presentSims: List<SimInfo> = emptyList(),
    val telecomAccounts: List<Account> = emptyList(),
    val bonuses: List<Bonus> = emptyList(),
    val loading: Boolean = false
)