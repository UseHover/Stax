package com.hover.stax.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.domain.use_case.accounts.GetAccountsUseCase
import com.hover.stax.domain.use_case.bonus.FetchBonusUseCase
import com.hover.stax.domain.use_case.bonus.GetBonusesUseCase
import com.hover.stax.domain.use_case.financial_tips.GetTipsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getBonusesUseCase: GetBonusesUseCase,
    private val fetchBonusUseCase: FetchBonusUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getTipsUseCase: GetTipsUseCase
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeState())
    val homeState = _homeState.asStateFlow()

    init {
        fetchBonuses()
    }

    private fun fetchBonuses() = viewModelScope.launch {
        fetchBonusUseCase()
    }

    fun getBonusList() = viewModelScope.launch {
        getBonusesUseCase().collect {
            _homeState.value = _homeState.value.copy(bonuses = it)
        }
    }

    fun getAccounts() = viewModelScope.launch {
        getAccountsUseCase().collect {
            _homeState.value = _homeState.value.copy(accounts = it)
        }
    }

    fun getFinancialTips() = viewModelScope.launch {
        getTipsUseCase().collect {
            _homeState.value = homeState.value.copy(financialTips = it)
        }
    }
}