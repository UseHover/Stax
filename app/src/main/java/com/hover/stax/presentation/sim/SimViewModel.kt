package com.hover.stax.presentation.sim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.sdk.sims.SimInfo
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.model.Bonus
import com.hover.stax.domain.use_case.accounts.CreateAccountsUseCase
import com.hover.stax.domain.use_case.accounts.GetAccountsUseCase
import com.hover.stax.domain.use_case.bonus.GetBonusesUseCase
import com.hover.stax.domain.use_case.sims.GetPresentSimUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SimViewModel(
    private val presentSimUseCase: GetPresentSimUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val createAccountsUseCase: CreateAccountsUseCase,
    private val bonusUseCase: GetBonusesUseCase
) : ViewModel() {

    private val _simUiState = MutableStateFlow(SimUiState())
    val simUiState = _simUiState.asStateFlow()

    init {
        fetchData()
    }

    private fun fetchData() {
        _simUiState.update { it.copy(loading = true) }

        fetchPresentSims()
        fetchBonuses()
    }

    private fun fetchPresentSims() = viewModelScope.launch {
        presentSimUseCase.presentSims.collect { sims ->
            _simUiState.update { it.copy(presentSims = sims) }

            setTelecomAccounts(sims.map { it.subscriptionId }.toIntArray())
        }
    }

    private fun setTelecomAccounts(subIds: IntArray) = viewModelScope.launch {
        getAccountsUseCase.telecomAccounts(subIds).collect { accounts ->
            _simUiState.update { it.copy(loading = false, telecomAccounts = accounts) }

            createAccountForSimsIfRequired(accounts, simUiState.value.presentSims)
        }
    }

    private suspend fun createAccountForSimsIfRequired(telecomAccounts: List<Account>, presentSims: List<SimInfo>) = viewModelScope.launch(Dispatchers.IO) {
        getSimsHavingNoTelecomAccount(telecomAccounts, presentSims).also {
            createAccountsUseCase.createTelecomAccounts(it)
        }
    }

    private fun getSimsHavingNoTelecomAccount(accounts: List<Account>, sims: List<SimInfo>): List<SimInfo> {
        return sims.filter { accounts.find { account -> account.simSubscriptionId == it.subscriptionId } == null }
    }

    private fun fetchBonuses() = viewModelScope.launch {
        bonusUseCase.bonusList.collect { bonusList ->
            _simUiState.update { it.copy(bonuses = bonusList) }
        }
    }

}