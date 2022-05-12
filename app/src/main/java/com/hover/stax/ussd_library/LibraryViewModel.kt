package com.hover.stax.ussd_library

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hover.sdk.api.Hover
import com.hover.sdk.sims.SimInfo
import com.hover.stax.channels.Channel
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.utils.Utils
import com.hover.stax.utils.isAbsolutelyEmpty
import com.hover.stax.utils.toFilteringStandard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import timber.log.Timber

class LibraryViewModel(val repo: DatabaseRepo, val application: Application) : ViewModel() {

    var allChannels: LiveData<List<Channel>> = MutableLiveData()
    val stagedChannels = MediatorLiveData<List<Channel>>()
    var sims: MutableLiveData<List<SimInfo>> = MutableLiveData()
    var country: MediatorLiveData<String> = MediatorLiveData()
    var filteredChannels = MediatorLiveData<List<Channel>>()
    var filterQuery = MutableLiveData<String>();

    private var simReceiver: BroadcastReceiver? = null

    init {
        simReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                viewModelScope.launch(Dispatchers.IO) {
                    sims.postValue(repo.presentSims)
                }
            }
        }

        country.value = null
        country.addSource(sims, this::pickFirstCountry)

        filterQuery.value = ""

        stagedChannels.addSource(country, this::filterChannels)
        stagedChannels.addSource(allChannels, this::filterChannels)

        filteredChannels.addSource(stagedChannels, this::filter)

        loadSims()
        allChannels = repo.allChannels
        stagedChannels.value = emptyList()
    }


    private fun loadSims() {
        viewModelScope.launch {
            sims.postValue(repo.presentSims)
        }

        simReceiver?.let {
            LocalBroadcastManager.getInstance(application)
                .registerReceiver(it, IntentFilter(Utils.getPackage(application) + ".NEW_SIM_INFO_ACTION"))
        }

        Hover.updateSimInfo(application)
    }

    private fun pickFirstCountry(sims: List<SimInfo>?) {
        if (!sims.isNullOrEmpty()) {
            country.postValue(sims.first().countryIso)
        }
    }

    fun setCountry(countryCode: String) = viewModelScope.launch(Dispatchers.IO) {
        country.postValue(countryCode)
    }

    private fun filterChannels(channels: List<Channel>?) = filter(channels, country.value)

    private fun filterChannels(countryCode: String?) = filter(allChannels.value, countryCode)

    private fun filter(channels: List<Channel>?, countryCode: String?) = viewModelScope.launch(Dispatchers.IO) {
        Timber.i("Filtering channels: $countryCode")

        if (countryCode == null || countryCode == CountryAdapter.CODE_ALL_COUNTRIES)
            channels?.let { stagedChannels.postValue(it) }
        else
            stagedChannels.postValue(repo.getChannelsByCountry(countryCode))
    }

    private fun filter(stagedChannels: List<Channel>) {
        if(stagedChannels.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                val filteredList = stagedChannels.filter { it.toString().toFilteringStandard()
                    .contains(filterQuery.value!!.toFilteringStandard()) }
                filteredChannels.postValue(filteredList)
            }
        }
    }

    fun isInSearchMode() : Boolean {
        return !filterQuery.value!!.isAbsolutelyEmpty()
    }

    fun runChannelFilter(value: String) {
        filterQuery.value = value
        filterChannels(stagedChannels.value!!)
    }

    override fun onCleared() {
        try {
            simReceiver?.let {
                LocalBroadcastManager.getInstance(application).unregisterReceiver(it)
            }
        } catch (ignored: Exception) {
        }
        super.onCleared()
    }
}