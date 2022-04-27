package com.hover.stax.addChannels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessaging
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.ActionApi
import com.hover.sdk.api.Hover
import com.hover.sdk.sims.SimInfo
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.AccountRepo
import com.hover.stax.actions.ActionRepo
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelRepo
import com.hover.stax.notifications.PushNotificationTopicsInterface
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants
import com.hover.stax.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber

class AddChannelsViewModel(application: Application, val repo: ChannelRepo, val accountRepo: AccountRepo, val actionRepo: ActionRepo) : AndroidViewModel(application),
    PushNotificationTopicsInterface {

    val allChannels: LiveData<List<Channel>> = repo.publishedChannels
    val selectedChannels: LiveData<List<Channel>> = repo.selected

    var sims = MutableLiveData<List<SimInfo>>()
    var simHniList: LiveData<List<String>> = MutableLiveData()

    var simChannels = MediatorLiveData<List<Channel>>()
    val filteredChannels = MediatorLiveData<List<Channel>>()
    val filterQuery = MutableLiveData<String>()

    val accounts = MutableLiveData<List<Account>>()

    private var simReceiver: BroadcastReceiver? = null

    init {
        updateAccounts()

        filterQuery.value = ""

        setSimBroadcastReceiver()
        loadSims()
        simHniList = Transformations.map(sims, this::getHnisAndSubscribeToFirebase)
        simChannels.apply {
            addSource(allChannels, this@AddChannelsViewModel::onChannelsUpdateHnis)
            addSource(simHniList, this@AddChannelsViewModel::onSimUpdate)
        }

        filteredChannels.addSource(allChannels, this@AddChannelsViewModel::filterSimChannels)
        filteredChannels.addSource(simChannels, this@AddChannelsViewModel::filterSimChannels)

        loadAccounts()
    }

    private fun loadSims() {
        viewModelScope.launch(Dispatchers.IO) {
            val deviceSims = repo.presentSims
            sims.postValue(deviceSims)

            val countryCodes = deviceSims.map { it.countryIso }.toSet()
            Utils.putStringSet(Constants.COUNTRIES, countryCodes, getApplication())
        }

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
                    sims.postValue(repo.presentSims)
                }
            }
        }
    }

    private fun onChannelsUpdateHnis(channels: List<Channel>) {
        updateSimChannels(simChannels, channels, simHniList.value)
    }

    private fun onSimUpdate(hniList: List<String>) {
        updateSimChannels(simChannels, allChannels.value, hniList)
    }

    private fun updateSimChannels(simChannels: MediatorLiveData<List<Channel>>, channels: List<Channel>?, hniList: List<String>?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (channels == null || hniList == null) return@launch

            val simChannelList = ArrayList<Channel>()

            for (i in channels.indices) {
                val hniArr = channels[i].hniList.split(",")

                for (s in hniArr) {
                    if (hniList.contains(s)) {
                        if (!simChannelList.contains(channels[i]))
                            simChannelList.add(channels[i])
                    }
                }
            }

            simChannels.postValue(simChannelList)
        }
    }

    private fun getHnisAndSubscribeToFirebase(sims: List<SimInfo>?): List<String>? {
        if (sims == null) return null

        val hniList = ArrayList<String>()

        for (sim in sims) {
            if (!hniList.contains(sim.osReportedHni) && sim.countryIso != null) {
                FirebaseMessaging.getInstance().subscribeToTopic("sim-".plus(sim.osReportedHni))
                FirebaseMessaging.getInstance().subscribeToTopic(sim.countryIso.uppercase())
                hniList.add(sim.osReportedHni)
            }
        }

        return hniList
    }

    fun setChannelsSelected(channels: List<Channel>?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (channels.isNullOrEmpty()) return@launch

            channels.forEachIndexed { index, channel ->
                logChoice(channel)
                channel.selected = true
                channel.defaultAccount = selectedChannels.value.isNullOrEmpty() && index == 0
                repo.update(channel)

                ActionApi.scheduleActionConfigUpdate(channel.countryAlpha2, 24, getApplication())
            }
        }
    }

    private fun logChoice(channel: Channel) {
        joinChannelGroup(channel.id, getApplication() as Context)
        val args = JSONObject()

        try {
            args.put((getApplication() as Context).getString(R.string.added_channel_id), channel.id)
        } catch (ignored: Exception) {
        }

        AnalyticsUtil.logAnalyticsEvent((getApplication() as Context).getString(R.string.new_channel_selected), args, getApplication() as Context)
    }

    private fun updateAccounts() = viewModelScope.launch(Dispatchers.IO) {
        val savedAccounts = accountRepo.getAllAccounts()
        if (savedAccounts.isNullOrEmpty()) return@launch

        if (savedAccounts.none { it.isDefault }) {
            val defaultChannel: Channel? = selectedChannels.value?.firstOrNull { it.defaultAccount }

            if (defaultChannel != null) {
                val ids = savedAccounts.filter { it.channelId == defaultChannel.id }.map { it.id }.toList()
                ids.minOrNull()?.let { id -> accountRepo.update(savedAccounts.first { it.id == id }) }
            }
        } else {
            Timber.e("Nothing to update. Default account set")
        }
    }

    fun loadAccounts() = viewModelScope.launch(Dispatchers.IO) {
        accountRepo.getAccounts().collect { accounts.postValue(it) }
    }

    @Deprecated(message = "Newer versions of the app don't need this", replaceWith = ReplaceWith(""), level = DeprecationLevel.WARNING)
    fun migrateAccounts() = viewModelScope.launch(Dispatchers.IO) {
        if (accounts.value.isNullOrEmpty() && !selectedChannels.value.isNullOrEmpty()) {
            createAccounts(selectedChannels.value!!)
        }
    }

    fun createAccounts(channels: List<Channel>) = viewModelScope.launch(Dispatchers.IO) {
        setChannelsSelected(channels)
        val defaultAccount = accountRepo.getDefaultAccount()

        channels.forEach {
            with(it) {
                val accountName: String = if (getFetchAccountAction(it.id) == null) name else Constants.PLACEHOLDER //placeholder alias for easier identification later
                val account = Account(accountName, name, logoUrl, accountNo, id, it.countryAlpha2, it.id, primaryColorHex, secondaryColorHex, defaultAccount == null)
                accountRepo.insert(account)
            }
        }
    }

    fun getFetchAccountAction(channelId: Int): HoverAction? = actionRepo.getActions(channelId, HoverAction.FETCH_ACCOUNTS).firstOrNull()

    private fun filterSimChannels(channels: List<Channel>) {
        if(!channels.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                val filteredList = channels.filter { toMatchingString(toFilterableString(it)).contains(toMatchingString(filterQuery.value!!)) }
                Timber.i("accounts matched size is: ${filteredList.size}")
                filteredChannels.postValue(filteredList)
            }
        }
    }

    fun runChannelFilter(value: String) {
        filterQuery.value = value
    }

    private fun toFilterableString(channel: Channel): String {
        return channel.name + channel.rootCode
    }

    fun filterSimChannels(value: String) {
        filterQuery.value = value
        val listToFilter : List<Channel>? = if(!simChannels.value.isNullOrEmpty()) simChannels.value else allChannels.value
        filterSimChannels(listToFilter!!)
    }

    private fun toMatchingString(value: String) : String {
        return value.lowercase().replace(" ", "");
    }
}