package com.hover.stax.presentation.financial_tips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.domain.use_case.financial_tips.GetTipsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FinancialTipsViewModel(private val getTipsUseCase: GetTipsUseCase) : ViewModel() {

    private val _tips = MutableStateFlow(FinancialTipsState())
    val tipsState = _tips.asStateFlow()

    fun getTips() = viewModelScope.launch {
        getTipsUseCase().collect {
            _tips.value = _tips.value.copy(tips = it)
        }
    }
}
