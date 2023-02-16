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
package com.hover.stax.addChannels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessaging
import com.hover.sdk.api.ActionApi
import com.hover.sdk.api.Hover
import com.hover.sdk.sims.SimInfo
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.data.local.SimRepo
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.data.local.channels.ChannelRepo
import com.hover.stax.domain.model.Account
import com.hover.stax.notifications.PushNotificationTopicsInterface
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Utils
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.hover.sdk.actions.HoverAction
import com.hover.stax.domain.model.USSDAccount
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber

class AddAccountViewModel(
    application: Application,
    val repo: ChannelRepo,
    val simRepo: SimRepo,
    val accountRepo: AccountRepo,
    val actionRepo: ActionRepo
) : AndroidViewModel(application),
    PushNotificationTopicsInterface {

    val accounts: LiveData<List<Account>> = accountRepo.getAllLiveAccounts()
    private val allChannels: LiveData<List<Channel>> = repo.publishedNonTelecomChannels

    var sims = MutableLiveData<List<SimInfo>>()
    var simCountryList: LiveData<List<String>> = MutableLiveData()

    var countryChoice: MediatorLiveData<String> = MediatorLiveData()
    val filterQuery = MutableLiveData<String?>()
    private var countryChannels = MediatorLiveData<List<Channel>>()
    val filteredChannels = MediatorLiveData<List<Channel>>()

    val _channelCountryList = MediatorLiveData<List<String>>()
    val channelCountryList: LiveData<List<String>> = _channelCountryList

    val chosenChannel = MutableSharedFlow<Channel>()
    val createdAccount = MutableSharedFlow<USSDAccount>()
    private val _balanceAction = MutableSharedFlow<HoverAction>()
    val balanceAction = _balanceAction.asSharedFlow()

    private val _accountCreatedEvent = MutableSharedFlow<Boolean>()
    val accountCreatedEvent = _accountCreatedEvent.asSharedFlow()

    private var simReceiver: BroadcastReceiver? = null

    init {
        setSimBroadcastReceiver()
        loadSims()

        simCountryList = Transformations.map(sims, this::getCountriesAndFirebaseSubscriptions)
        countryChoice.addSource(simCountryList, this@AddAccountViewModel::onSimUpdate)

        countryChannels.apply {
            addSource(allChannels, this@AddAccountViewModel::onAllChannelsUpdate)
            addSource(countryChoice, this@AddAccountViewModel::onCountryUpdate)
        }

        _channelCountryList.addSource(allChannels, this::loadChannelCountryList)

        filteredChannels.addSource(filterQuery, this@AddAccountViewModel::search)
        filteredChannels.addSource(countryChannels, this@AddAccountViewModel::updateCountryChannels)
    }

    private fun loadSims() {
        viewModelScope.launch(Dispatchers.IO) { sims.postValue(simRepo.getPresentSims()) }

        simReceiver?.let {
            LocalBroadcastManager.getInstance(getApplication())
                .registerReceiver(it, IntentFilter(Utils.getPackage(getApplication()).plus(".NEW_SIM_INFO_ACTION")))
        }

        Hover.updateSimInfo(getApplication())
    }

    private fun setSimBroadcastReceiver() {
        simReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                viewModelScope.launch {
                    sims.postValue(simRepo.getPresentSims())
                }
            }
        }
    }

    private fun onAllChannelsUpdate(channels: List<Channel>?) {
        updateCountryChannels(channels, countryChoice.value)
    }

    private fun onCountryUpdate(countryCode: String?) {
        updateCountryChannels(allChannels.value, countryCode)
    }

    private fun loadChannelCountryList(channels: List<Channel>) = viewModelScope.launch {
        val countryCodes = mutableListOf(CountryAdapter.CODE_ALL_COUNTRIES)
        countryCodes.addAll(channels.map { it.countryAlpha2 }.distinct().sorted())

        _channelCountryList.postValue(countryCodes)
    }

    private fun onSimUpdate(countryCodes: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (countryCodes.isNotEmpty()) {
                for (code in countryCodes) {
                    if (repo.getChannelsByCountry(code).isNotEmpty())
                        updateCountry(code)
                }
            }
        }
    }

    private fun updateCountryChannels(channels: List<Channel>?, countryCode: String?) {
        countryChannels.value = when {
            countryCode.isNullOrEmpty() || countryCode == CountryAdapter.CODE_ALL_COUNTRIES -> channels
            else -> channels?.filter { it.countryAlpha2 == countryChoice.value }
        }
    }

    private fun getCountriesAndFirebaseSubscriptions(sims: List<SimInfo>?): List<String>? {
        setFirebaseSubscriptions(sims)
        return sims?.map { it.countryIso }
    }

    private fun setFirebaseSubscriptions(sims: List<SimInfo>?) {
        if (sims == null) return

        val hniList = ArrayList<String>()

        for (sim in sims) {
            if (!hniList.contains(sim.osReportedHni) && sim.countryIso != null) {
                FirebaseMessaging.getInstance().subscribeToTopic("sim-".plus(sim.osReportedHni))
                FirebaseMessaging.getInstance().subscribeToTopic(sim.countryIso.uppercase())
                hniList.add(sim.osReportedHni)
            }
        }
    }

    private fun logChoice(account: USSDAccount) {
        joinChannelGroup(account.channelId, getApplication() as Context)
        val args = JSONObject()

        try {
            args.put((getApplication() as Context).getString(R.string.added_channel_id), account.channelId)
        } catch (ignored: Exception) {
        }

        AnalyticsUtil.logAnalyticsEvent((getApplication() as Context).getString(R.string.new_channel_selected), args, getApplication() as Context)
    }

    fun chooseChannel(channel: Channel) = viewModelScope.launch(Dispatchers.IO) {
        chosenChannel.emit(channel)
    }

    fun createAccountWithoutBalance(channel: Channel) = viewModelScope.launch(Dispatchers.IO) {
        createAccount(channel)

        _accountCreatedEvent.emit(true)
    }

    fun createAccount(channel: Channel) = viewModelScope.launch(Dispatchers.IO) {
        val defaultAccount = accountRepo.getDefaultAccount()

        val account = USSDAccount(channel, defaultAccount == null, -1)
        logChoice(account)
        ActionApi.scheduleActionConfigUpdate(channel.countryAlpha2, 24, getApplication())

        val accountId = accountRepo.insert(account)
        Timber.e("Created account with id %s", accountId)
        createdAccount.emit(account)
    }

    fun balanceCheck(channel: Channel) = viewModelScope.launch(Dispatchers.IO) {
        val action = actionRepo.getFirstAction(channel.institutionId, channel.countryAlpha2, HoverAction.BALANCE)

        createAccount(channel)
        action?.let { _balanceAction.emit(action) } ?: run {
            Timber.e("something went wrong")
//            _actionRunError.send((getApplication() as Context).getString(R.string.error_running_action))
        }
    }

    fun updateSearch(value: String?) {
        filterQuery.value = value
    }

    private fun search(value: String?) {
        countryChannels.value?.let { runFilter(it, value) }
    }

    fun updateCountry(code: String) {
        countryChoice.postValue(code.lowercase())
    }

    private fun updateCountryChannels(channels: List<Channel>?) {
        channels?.let { runFilter(it, filterQuery.value) }
    }

    private fun runFilter(channels: List<Channel>, value: String?) {
        filteredChannels.value = channels.filter { standardizeString(it.toString()).contains(standardizeString(value)) }
    }

    fun updateChannel(channel: Channel) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.update(channel)
        }
    }

    fun loadChannel(channelId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val c = repo.getChannel(channelId)
            c?.let { chosenChannel.emit(c) }
        }
    }

    private fun standardizeString(value: String?): String {
        // a non null String always contains an empty string
        if (value == null) return ""
        return value.lowercase().replace(" ", "").replace("#", "").replace("-", "")
    }

    override fun onCleared() {
        try {
            simReceiver?.let {
                LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(it)
            }
        } catch (ignored: Exception) {
        }
        super.onCleared()
    }
}