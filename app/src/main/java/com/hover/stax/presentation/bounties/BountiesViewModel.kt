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
import com.hover.stax.domain.model.ChannelBounties
import com.hover.stax.domain.model.Resource
import com.hover.stax.domain.use_case.bounties.GetChannelBountiesUseCase
import com.hover.stax.domain.use_case.channels.GetPresentSimsUseCase
import com.hover.stax.utils.Utils.getPackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BountiesViewModel(private val simsUseCase: GetPresentSimsUseCase, private val bountiesUseCase: GetChannelBountiesUseCase, val application: Application) : ViewModel() {

    var countryList = MutableStateFlow<List<String>>(emptyList())
        private set

    var sims = MutableStateFlow<List<SimInfo>>(emptyList())
        private set

    var bountiesState = MutableStateFlow(BountiesState())
        private set

    var country = MutableStateFlow(CountryAdapter.CODE_ALL_COUNTRIES)
        private set

    private var simReceiver: BroadcastReceiver? = null

    init {
        simReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                fetchSims()
            }
        }

        loadBountyData()
    }

    private fun loadBountyData() {
        loadSims()
        loadCountryList()
        loadBounties()
    }

    private fun loadCountryList() = viewModelScope.launch {
        bountiesUseCase.getChannelList().collect { codes ->
            countryList.update { codes }
        }
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
        sims.update { it }
    }

    fun loadBounties(countryCode: String = CountryAdapter.CODE_ALL_COUNTRIES) {
        country.value = countryCode
        bountiesUseCase.getBounties(countryCode).onEach { result ->
            when (result) {
                is Resource.Loading -> bountiesState.update { it.copy(loading = true) }
                is Resource.Error -> bountiesState.update { it.copy(loading = false, error = result.message!!) }
                is Resource.Success -> bountiesState.update { it.copy(loading = false, bounties = result.data!!) }
            }
        }.launchIn(viewModelScope)
    }

    private fun emitSlowly(channelBounties: List<ChannelBounties>) = viewModelScope.launch(Dispatchers.IO) {
        channelBounties.chunked(20).forEach { list ->
            delay(1000)

            bountiesState.update { it.copy(loading = false, bounties = list) }
        }
    }

    fun isSimPresent(bounty: Bounty): Boolean = simsUseCase.simPresent(bounty, sims.value)

    override fun onCleared() {
        try {
            simReceiver?.let { LocalBroadcastManager.getInstance(application).unregisterReceiver(it) }
        } catch (ignored: Exception) {
        }
        super.onCleared()
    }
}