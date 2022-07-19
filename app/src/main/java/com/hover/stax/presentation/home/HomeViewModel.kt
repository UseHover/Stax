package com.hover.stax.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.domain.model.Resource
import com.hover.stax.domain.use_case.accounts.GetAccountsUseCase
import com.hover.stax.domain.use_case.bonus.FetchBonusUseCase
import com.hover.stax.domain.use_case.bonus.GetBonusesUseCase
import com.hover.stax.domain.use_case.financial_tips.GetTipsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
        fetchData()
    }

    private fun fetchData() {
        getBonusList()
        getAccounts()
        getFinancialTips()
    }

    private fun fetchBonuses() = viewModelScope.launch {
        fetchBonusUseCase()
    }

    private fun getBonusList() = viewModelScope.launch {
        getBonusesUseCase.bonusList.collect {
            _homeState.value = _homeState.value.copy(bonuses = it)
        }
    }

    private fun getAccounts() = viewModelScope.launch {
        getAccountsUseCase.accounts.collect {
            _homeState.value = _homeState.value.copy(accounts = it)
        }
    }

    private fun getFinancialTips() = getTipsUseCase().onEach { result ->
        if (result is Resource.Success)
            _homeState.value = homeState.value.copy(financialTips = result.data ?: emptyList())
    }.launchIn(viewModelScope)
}