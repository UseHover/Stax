package com.hover.stax.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.channels.Channel
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.database.DatabaseRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LibraryViewModel(val repo: DatabaseRepo) : ViewModel() {

    var allChannels: LiveData<List<Channel>> = MutableLiveData()
    val filteredChannels = MutableLiveData<List<Channel>>()

    init {
        allChannels = repo.allChannels
        filteredChannels.value = allChannels.value
    }

    fun filterChannels(countryCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (countryCode == CountryAdapter.codeRepresentingAllCountries())
                filteredChannels.postValue(allChannels.value)
            else
                filteredChannels.postValue(repo.getChannelsByCountry(countryCode))
        }
    }
}