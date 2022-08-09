package com.hover.stax.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.model.Resource
import com.hover.stax.domain.use_case.accounts.GetAccountsUseCase
import com.hover.stax.domain.use_case.bonus.FetchBonusUseCase
import com.hover.stax.domain.use_case.bonus.GetBonusesUseCase
import com.hover.stax.domain.use_case.financial_tips.TipsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getBonusesUseCase: GetBonusesUseCase,
    private val fetchBonusUseCase: FetchBonusUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val tipsUseCase: TipsUseCase
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeState())
    val homeState = _homeState.asStateFlow()

    private val _accounts = MutableLiveData<List<Account>>()
    val accounts: LiveData<List<Account>> = _accounts

    init {
        fetchBonuses()
        fetchData()
    }

    private fun fetchData() {
        getBonusList()
        getAccounts()
        getFinancialTips()
        getDismissedFinancialTips()
    }

    private fun fetchBonuses() = viewModelScope.launch {
        fetchBonusUseCase()
    }

    private fun getBonusList() = viewModelScope.launch {
        getBonusesUseCase.bonusList.collect { bonusList ->
            _homeState.update { it.copy(bonuses = bonusList) }
        }
    }

    private fun getAccounts() = viewModelScope.launch {
        getAccountsUseCase.accounts.collect { accounts ->
            _homeState.update { it.copy(accounts = accounts) }
            _accounts.postValue(accounts)
        }
    }

    private fun getFinancialTips() = tipsUseCase().onEach { result ->
        if (result is Resource.Success)
            _homeState.update { it.copy(financialTips = result.data ?: emptyList()) }
    }.launchIn(viewModelScope)

    private fun getDismissedFinancialTips() = _homeState.update {
        it.copy(dismissedTipId = tipsUseCase.getDismissedTipId() ?: "")
    }

    fun dismissTip(id: String) {
        viewModelScope.launch {
            tipsUseCase.dismissTip(id)
           _homeState.update { it.copy(dismissedTipId = id) }
        }
    }
}