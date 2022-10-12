package com.hover.stax.presentation.sim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.domain.use_case.accounts.CreateAccountsUseCase
import com.hover.stax.domain.use_case.accounts.GetAccountsUseCase
import com.hover.stax.domain.use_case.bonus.GetBonusesUseCase
import com.hover.stax.domain.use_case.sims.GetPresentSimUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class SimViewModel(
    private val presentSimUseCase: GetPresentSimUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val createAccountsUseCase: CreateAccountsUseCase,
    private val bonusUseCase: GetBonusesUseCase
) : ViewModel() {

    private val _simUiState = MutableStateFlow(SimUiState())
    val simUiState = _simUiState.asStateFlow()

    init {
        _simUiState.update { it.copy(loading = true) }
        collectPresentSims()
        saveTelecomAccounts()
        collectTelecomAccounts()
        fetchBonuses()
    }

    private fun collectPresentSims() = viewModelScope.launch {
        presentSimUseCase.presentSims.collectLatest { sims ->
            _simUiState.update { it.copy(presentSims = sims,) }
        }
    }
    private fun saveTelecomAccounts() = viewModelScope.launch(Dispatchers.IO) {
        simUiState.collectLatest {
            createAccountsUseCase.createTelecomAccounts(it.presentSims)
        }
    }

    private fun collectTelecomAccounts() = viewModelScope.launch {
        getAccountsUseCase.telecomAccounts().collectLatest { accounts ->
            accounts.forEach{
                Timber.i("Collected telecom account with simSubscription: ${it.simSubscriptionId} and name ${it.name}")
            }
            _simUiState.update { it.copy(loading = false, telecomAccounts = accounts) }
        }
    }

    private fun fetchBonuses() = viewModelScope.launch {
        bonusUseCase.bonusList.collect { bonusList ->
            _simUiState.update { it.copy(bonuses = bonusList) }
        }
    }

}