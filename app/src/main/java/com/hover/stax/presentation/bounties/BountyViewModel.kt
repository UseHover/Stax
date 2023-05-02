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
package com.hover.stax.presentation.bounties

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hover.sdk.api.Hover
import com.hover.sdk.sims.SimInfo
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.domain.model.Bounty
import com.hover.stax.domain.model.Resource
import com.hover.stax.domain.use_case.bounties.GetChannelBountiesUseCase
import com.hover.stax.data.sim.SimInfoRepository
import com.hover.stax.utils.Utils.getPackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BountyViewModel(
    private val simRepository: com.hover.stax.data.sim.SimInfoRepository,
    private val bountiesUseCase: GetChannelBountiesUseCase,
    val application: Application
) : ViewModel() {

    private val _countryList = MutableStateFlow<List<String>>(emptyList())
    val countryList = _countryList.asStateFlow()

    private val _sims = MutableStateFlow<List<SimInfo>>(emptyList())
    val sims = _sims.asStateFlow()

    private val _bountiesState = MutableStateFlow(BountiesState())
    val bountiesState = _bountiesState.asStateFlow()

    private val _country = MutableStateFlow(CountryAdapter.CODE_ALL_COUNTRIES)
    val country = _country.asStateFlow()

    private val onBountySelectEvent = Channel<BountySelectEvent>()
    val bountySelectEvent = onBountySelectEvent.receiveAsFlow()

    private var simReceiver: BroadcastReceiver? = null

    init {
        simReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                fetchSims()
            }
        }

        loadSims()
        loadCountryList()
        loadBounties()
    }

    private fun loadSims() {
        fetchSims()

        simReceiver?.let {
            LocalBroadcastManager.getInstance(application)
                .registerReceiver(it, IntentFilter(getPackage(application) + ".NEW_SIM_INFO_ACTION"))
        }
        Hover.updateSimInfo(application)
    }

    private fun fetchSims() = viewModelScope.launch(Dispatchers.IO) {
        _sims.update { simRepository.getPresentSims() }
    }

    private fun loadCountryList() = viewModelScope.launch {
        bountiesUseCase.getCountryList().collect { codes ->
            _countryList.update { codes }
        }
    }

    fun loadBounties(countryCode: String = CountryAdapter.CODE_ALL_COUNTRIES) {
        _country.value = countryCode
        bountiesUseCase.getBounties(countryCode).onEach { result ->
            when (result) {
                is Resource.Loading -> _bountiesState.update { it.copy(loading = true) }
                is Resource.Error -> _bountiesState.update { it.copy(loading = false, error = result.message!!) }
                is Resource.Success -> _bountiesState.update { it.copy(loading = false, bounties = result.data!!) }
            }
        }.launchIn(viewModelScope)
    }

    fun isSimPresent(bounty: Bounty): Boolean = bountiesUseCase.isSimPresent(bounty, sims.value)

    fun handleBountyEvent(bountySelectEvent: BountySelectEvent?) = viewModelScope.launch {
        bountySelectEvent?.let { onBountySelectEvent.send(bountySelectEvent) }
    }

    override fun onCleared() {
        try {
            simReceiver?.let { LocalBroadcastManager.getInstance(application).unregisterReceiver(it) }
        } catch (ignored: Exception) {
        }
        super.onCleared()
    }
}