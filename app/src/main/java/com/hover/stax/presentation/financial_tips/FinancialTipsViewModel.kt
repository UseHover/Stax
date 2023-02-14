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

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hover.stax.domain.model.FinancialTip
import com.hover.stax.domain.repository.FinancialTipsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

open class FinancialTipsViewModel(application: Application, private val tipsRepo: FinancialTipsRepository) : AndroidViewModel(application) {

    val tips = MutableLiveData<List<FinancialTip>>()

    private val _tipsState = MutableStateFlow(FinancialTipsState())
    val tipsState = _tipsState.asStateFlow()

    init {
        viewModelScope.launch {
            tips.postValue(tipsRepo.getTips())
        }
    }

//    private fun loadTips(): Flow<Resource<List<FinancialTip>>> = flow {
//        try {
//            emit(Resource.Loading())
//
//            val financialTips = tipsRepo.tips
//            emit(Resource.Success(financialTips))
//        } catch (e: Exception) {
//            emit(Resource.Error("Error fetching tips"))
//        }
//    }
//
//    fun getTips() = loadTips().onEach { result ->
//        when (result) {
//            is Resource.Loading -> _tipsState.value = FinancialTipsState(isLoading = true)
//            is Resource.Error -> _tipsState.value = FinancialTipsState(error = result.message ?: "An unexpected error occurred", isLoading = false)
//            is Resource.Success -> _tipsState.value = FinancialTipsState(tips = result.data ?: emptyList(), isLoading = false)
//        }
//    }.launchIn(viewModelScope)
}