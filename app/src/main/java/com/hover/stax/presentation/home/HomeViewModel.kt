package com.hover.stax.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.data.Resource
import com.hover.stax.domain.use_case.bonus.FetchBonusUseCase
import com.hover.stax.domain.use_case.bonus.GetBonusesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HomeViewModel(private val getBonusesUseCase: GetBonusesUseCase, private val fetchBonusUseCase: FetchBonusUseCase) : ViewModel() {

    private val _bonusList = MutableStateFlow<HomeState>(HomeState.BonusList(emptyList()))
    val bonusList = _bonusList.asStateFlow()

    init {
        fetchBonuses()
    }

    private fun fetchBonuses() = viewModelScope.launch {
        fetchBonusUseCase.invoke()
    }

    fun getBonusList() = getBonusesUseCase.getBonusList().onEach { result ->
        when (result) {
            is Resource.Loading -> _bonusList.value = HomeState.Loading
            is Resource.Success -> _bonusList.value = HomeState.BonusList(result.data!!)
            is Resource.Error -> _bonusList.value = HomeState.Error("No bonuses found")
        }
    }.launchIn(viewModelScope)
}