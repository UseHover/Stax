package com.hover.stax.library

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent
import timber.log.Timber
import java.lang.Exception

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = KoinJavaComponent.get(DatabaseRepo::class.java)

    var allChannels: LiveData<List<Channel>> = MutableLiveData()
    val filteredChannels = MediatorLiveData<List<Channel>>()
    var sims: MutableLiveData<List<SimInfo>> = MutableLiveData()
    var country: MediatorLiveData<String> = MediatorLiveData()

    init {
        country.value = null
        country.addSource(sims, this::pickFirstCountry)
        filteredChannels.addSource(allChannels, this::filterChannels)
        filteredChannels.addSource(country, this::filterChannels)
        loadSims()
        allChannels = repo.allChannels
        filteredChannels.value = null
    }

    private fun loadSims() {
        viewModelScope.launch {
            sims.postValue(repo.presentSims)
        }

        LocalBroadcastManager.getInstance(getApplication())
            .registerReceiver(simReceiver, IntentFilter(Utils.getPackage(getApplication()) + ".NEW_SIM_INFO_ACTION"))
        Hover.updateSimInfo(getApplication())
    }

    private fun pickFirstCountry(sims: List<SimInfo>?) {
        if (sims != null) {
            Timber.e("Picking first country: %s", sims.first().countryIso)
            country.postValue(sims.first().countryIso)
        }
    }

    fun setCountry(countryCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Timber.e("Updating country: %s", countryCode)
            country.postValue(countryCode)
        }
    }

    private val simReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModelScope.launch(Dispatchers.IO) {
                sims.postValue(repo.presentSims)
            }
        }
    }

    private fun filterChannels(channels: List<Channel>?) {
        filterChannels(channels, country.value)
    }

    private fun filterChannels(countryCode: String?) {
        filterChannels(allChannels.value, countryCode)
    }

    private fun filterChannels(channels: List<Channel>?, countryCode: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            Timber.e("Filtering channels %s", countryCode)
            if (countryCode == null || countryCode == CountryAdapter.codeRepresentingAllCountries())
                filteredChannels.postValue(channels)
            else
                filteredChannels.postValue(repo.getChannelsByCountry(countryCode))
        }
    }

    override fun onCleared() {
        try {
            LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(simReceiver)
        } catch (ignored: Exception) {}
        super.onCleared()
    }
}