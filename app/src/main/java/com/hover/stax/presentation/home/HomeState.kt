package com.hover.stax.presentation.home

import com.hover.stax.domain.model.Bonus

sealed class HomeState {
    object Loading : HomeState()
    data class BonusList(val bonuses: List<Bonus>) : HomeState()
    data class Error(val message: String) : HomeState()
}
