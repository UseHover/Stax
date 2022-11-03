package com.hover.stax.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.sdk.actions.HoverAction
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.model.Resource
import com.hover.stax.domain.repository.AccountRepository
import com.hover.stax.domain.use_case.financial_tips.TipsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(
    private val accountsRepo: AccountRepository,
    private val actionRepo: ActionRepo,
    private val tipsUseCase: TipsUseCase
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeState())
    val homeState = _homeState.asStateFlow()

    init {
        fetchData()
    }

    private fun fetchData() {
        getAccounts()
        getFinancialTips()
        getDismissedFinancialTips()
    }

    private fun getAccounts() = viewModelScope.launch {
        accountsRepo.addedAccounts.collect { accounts ->
            _homeState.update { it.copy(accounts = accounts) }
            getBonusList(accounts.map { it.countryAlpha2!! }.toTypedArray())
        }
    }

    private fun getBonusList(countries: Array<String>) = viewModelScope.launch {
        bonusListToFlow(countries).collect { bonusList ->
            if (bonusList is Resource.Success)
                _homeState.update { it.copy(bonuses = bonusList.data ?: emptyList()) }
        }
    }

    private fun bonusListToFlow(countries: Array<String>): Flow<Resource<List<HoverAction>>> = flow {
        Timber.e("Looking for bounties from: ${countries.contentToString()}")
        try {
            emit(Resource.Loading())

            emit(Resource.Success(actionRepo.getBonusActionsByCountry(countries)))
        } catch (e: Exception) {
            emit(Resource.Error("Error fetching tips"))
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