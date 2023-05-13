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
package com.hover.stax.domain.use_case.financial_tips

import com.hover.stax.data.tips.FinancialTipsRepository
import com.hover.stax.model.FinancialTip
import com.hover.stax.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TipsUseCase @Inject constructor(
    private val financialTipsRepository: FinancialTipsRepository
) {

    operator fun invoke(): Flow<com.hover.stax.model.Resource<List<com.hover.stax.model.FinancialTip>>> =
        flow {
            try {
                emit(com.hover.stax.model.Resource.Loading())

                val financialTips = financialTipsRepository.getTips()
                emit(com.hover.stax.model.Resource.Success(financialTips))
            } catch (e: Exception) {
                emit(com.hover.stax.model.Resource.Error("Error fetching tips"))
            }
        }

    fun getDismissedTipId(): String? = financialTipsRepository.getDismissedTipId()

    fun dismissTip(id: String) {
        financialTipsRepository.dismissTip(id)
    }
}