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
package com.hover.stax.presentation.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.domain.model.FinancialTip
import com.hover.stax.domain.model.Resource
import com.hover.stax.domain.repository.FinancialTipsRepository
import com.hover.stax.domain.use_case.AccountWithBalance
import com.hover.stax.presentation.bounties.BountiesState
import com.hover.stax.presentation.financial_tips.FinancialTipsViewModel
import com.hover.stax.utils.AnalyticsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(application: Application,
    actionRepo: ActionRepo,
    tipsRepo: FinancialTipsRepository
) : FinancialTipsViewModel(application, tipsRepo) {

    private val _addAccountEvent = Channel<Boolean>()
    val addAccountEvent = _addAccountEvent.receiveAsFlow()

    private val _accountDetail = Channel<AccountWithBalance>()
    val accountDetail = _accountDetail.receiveAsFlow()

    val bonusActions: LiveData<List<HoverAction>> = actionRepo.getBonusActions()

//    private fun bonusListToFlow(countries: Array<String>): Flow<Resource<List<HoverAction>>> = flow {
//        Timber.e("Looking for bounties from: ${countries.contentToString()}")
//        try {
//            emit(Resource.Loading())
//
//            emit(Resource.Success(actionRepo.getBonusActions()))
//        } catch (e: Exception) {
//            emit(Resource.Error("Error fetching tips"))
//        }
//    }

    fun requestAddAccount() = viewModelScope.launch(Dispatchers.IO) {
        Timber.e("requesting add account")
        _addAccountEvent.send(true)
    }

    fun requestGoToAccount(accountWithBalance: AccountWithBalance) = viewModelScope.launch(Dispatchers.IO) {
        Timber.e("requesting add account")
        _accountDetail.send(accountWithBalance)
    }

    fun logBuyAirtimeFromAd() = viewModelScope.launch(Dispatchers.IO) {
        AnalyticsUtil.logAnalyticsEvent((getApplication() as Context).getString(R.string.clicked_bonus_airtime_banner), getApplication())
    }
}