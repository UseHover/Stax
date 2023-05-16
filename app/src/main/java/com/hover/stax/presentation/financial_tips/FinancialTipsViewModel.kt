/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.presentation.financial_tips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.model.Resource
import com.hover.stax.domain.use_case.financial_tips.TipsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class FinancialTipsViewModel @Inject constructor(
    private val tipsUseCase: TipsUseCase
) : ViewModel() {

    private val _tipsState = MutableStateFlow(FinancialTipsState())
    val tipsState = _tipsState.asStateFlow()

    init {
        getTips()
    }

    fun getTips() = tipsUseCase().onEach { result ->
        when (result) {
            is com.hover.stax.model.Resource.Loading -> _tipsState.value = FinancialTipsState(isLoading = true)
            is com.hover.stax.model.Resource.Error -> _tipsState.value = FinancialTipsState(error = result.message ?: "An unexpected error occurred", isLoading = false)
            is com.hover.stax.model.Resource.Success -> _tipsState.value = FinancialTipsState(tips = result.data ?: emptyList(), isLoading = false)
        }
    }.launchIn(viewModelScope)
}