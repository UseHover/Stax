package com.hover.stax.presentation.financial_tips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.domain.model.Resource
import com.hover.stax.domain.use_case.financial_tips.GetTipsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class FinancialTipsViewModel(private val getTipsUseCase: GetTipsUseCase) : ViewModel() {

    private val _tipsState = MutableStateFlow(FinancialTipsState())
    val tipsState = _tipsState.asStateFlow()

    init {
        getTips()
    }

    fun getTips() = getTipsUseCase().onEach { result ->
        when (result) {
            is Resource.Loading -> _tipsState.value = FinancialTipsState(isLoading = true)
            is Resource.Error -> _tipsState.value = FinancialTipsState(error = result.message ?: "An unexpected error occurred", isLoading = false)
            is Resource.Success -> _tipsState.value = FinancialTipsState(tips = result.data ?: emptyList(), isLoading = false)
        }
    }.launchIn(viewModelScope)
}
