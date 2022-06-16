package com.hover.stax.presentation.home

import com.hover.stax.accounts.Account
import com.hover.stax.domain.model.Bonus

data class HomeState (
    val bonuses: List<Bonus> = emptyList(),
    val accounts: List<Account> = emptyList()
)
