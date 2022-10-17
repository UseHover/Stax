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

class HomeViewModel(
    private val accountsRepo: AccountRepository,
    private val actionRepo: ActionRepo,
    private val tipsUseCase: TipsUseCase
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeState())
    val homeState = _homeState.asStateFlow()

    private val _accounts = MutableLiveData<List<Account>>()
    val accounts: LiveData<List<Account>> = _accounts

    init {
        fetchData()
    }

    private fun fetchData() {
        getBonusList()
        getAccounts()
        getFinancialTips()
        getDismissedFinancialTips()
    }

    private fun getBonusList() = viewModelScope.launch {
        bonusListToFlow().collect { bonusList ->
            if (bonusList is Resource.Success)
                _homeState.update { it.copy(bonuses = bonusList.data ?: emptyList()) }
        }
    }

    private fun bonusListToFlow(): Flow<Resource<List<HoverAction>>> = flow {
        try {
            emit(Resource.Loading())

            emit(Resource.Success(actionRepo.bonusActions))
        } catch (e: Exception) {
            emit(Resource.Error("Error fetching tips"))
        }
    }

    private fun getAccounts() = viewModelScope.launch {
        accountsRepo.fetchAccounts.collect { accounts ->
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