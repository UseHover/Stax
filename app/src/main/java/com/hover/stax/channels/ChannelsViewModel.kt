package com.hover.stax.channels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessaging
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.sims.SimInfo
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.AccountDropdown
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.pushNotification.PushNotificationTopicsInterface
import com.hover.stax.requests.Request
import com.hover.stax.schedules.Schedule
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber

//TODO improve performance
class ChannelsViewModel(val application: Application, val repo: DatabaseRepo) : ViewModel(),
        ChannelDropdown.HighlightListener, AccountDropdown.HighlightListener, PushNotificationTopicsInterface {

    private var type = MutableLiveData<String>()
    var sims = MutableLiveData<List<SimInfo>>()
    var simHniList: LiveData<List<String>> = MutableLiveData()
    var allChannels: LiveData<List<Channel>> = MutableLiveData()
    var selectedChannels: LiveData<List<Channel>> = MutableLiveData()

    var simChannels = MediatorLiveData<List<Channel>>()
    val activeChannel = MediatorLiveData<Channel>()
    val channelActions = MediatorLiveData<List<HoverAction>>()
    val accounts = MediatorLiveData<List<Account>>()
    val activeAccount = MutableLiveData<Account>()
    private var simReceiver: BroadcastReceiver? = null

    init {
        simReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                viewModelScope.launch {
                    sims.postValue(repo.presentSims)
                }
            }
        }

        loadChannels()
        loadSims()

        simHniList = Transformations.map(sims, this::getHnisAndSubscribeToFirebase)
        simChannels.apply {
            addSource(allChannels, this@ChannelsViewModel::onChannelsUpdateHnis)
            addSource(simHniList, this@ChannelsViewModel::onSimUpdate)
        }

        activeChannel.addSource(selectedChannels, this::setActiveChannelIfNull)

        accounts.addSource(selectedChannels, this::loadAccounts)

        channelActions.apply {
            addSource(type, this@ChannelsViewModel::loadActions)
            addSource(selectedChannels, this@ChannelsViewModel::loadActions)
            addSource(activeChannel, this@ChannelsViewModel::loadActions)
        }
    }

    fun setType(t: String) {
        type.value = t
    }

    fun getActionType(): String = type.value!!

    private fun loadChannels() {
        removeStaleChannels()

        viewModelScope.launch {
            allChannels = repo.publishedChannels
            selectedChannels = repo.selected

            updateAccounts()
        }
    }

    /**
     * A prerequisite for actions to be loaded and run is having channels marked as selected. While adding channels,
     * this must be done before accounts can be created from fetch account actions or check balance. However, when accounts are not fetched,
     * the channel is still marked as selected. Since there is no clean way of handling this after the result of the transaction, this method
     * handles that for now.
     */
    private fun removeStaleChannels() {
        viewModelScope.launch(Dispatchers.IO) {
            val channels = repo.getChannelsAndAccounts()

            channels.forEach {
                if (it.accounts.isEmpty()) {
                    val c = it.channel
                    c.selected = false
                    repo.update(c)
                }
            }
        }
    }

    private fun loadSims() {
        viewModelScope.launch {
            sims.postValue(repo.presentSims)
        }

        simReceiver?.let {
            LocalBroadcastManager.getInstance(application)
                    .registerReceiver(it, IntentFilter(Utils.getPackage(application).plus(".NEW_SIM_INFO_ACTION")))
        }

        Hover.updateSimInfo(application)
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

    private fun onChannelsUpdateHnis(channels: List<Channel>) {
        updateSimChannels(simChannels, channels, simHniList.value)
    }

    private fun onSimUpdate(hniList: List<String>) {
        updateSimChannels(simChannels, allChannels.value, hniList)
    }

    private fun updateSimChannels(simChannels: MediatorLiveData<List<Channel>>, channels: List<Channel>?, hniList: List<String>?) {
        viewModelScope.launch {
            if (channels == null || hniList == null) return@launch

            val simChannelList = ArrayList<Channel>()

            for (i in channels.indices) {
                val hniArr = channels[i].hniList.split(",")

                for (s in hniArr) {
                    if (hniList.contains(Utils.stripHniString(s))) {
                        if (!simChannelList.contains(channels[i]))
                            simChannelList.add(channels[i])
                    }
                }
            }

            simChannels.value = simChannelList
        }
    }

    private fun setActiveChannelIfNull(channels: List<Channel>) {
        if (!channels.isNullOrEmpty() && activeChannel.value == null)
            activeChannel.value = channels.firstOrNull { it.defaultAccount }
    }

    fun setActiveChannel(channel: Channel) {
        activeChannel.postValue(channel)
    }

    fun setActiveAccount(account: Account?) {
        activeAccount.postValue(account)
    }

    private fun setActiveChannel(actions: List<HoverAction>) {
        if (actions.isNullOrEmpty()) return

        activeChannel.removeSource(channelActions)

        viewModelScope.launch(Dispatchers.IO) {
            val channelAccounts = repo.getChannelAndAccounts(actions.first().channel_id)
            channelAccounts?.let {
                activeChannel.postValue(it.channel)
                setActiveAccount(it.accounts.firstOrNull())
            }
        }
    }

    override fun highlightChannel(c: Channel?) {
        c?.let { setActiveChannel(it) }
    }

    private fun loadActions(t: String?) {
        if (t == null) return

        if ((t == HoverAction.BALANCE && selectedChannels.value == null) || (t != HoverAction.BALANCE && activeChannel.value == null)) return
        if (t == HoverAction.BALANCE) loadActions(selectedChannels.value!!, t) else loadActions(activeChannel.value!!, t)
    }

    private fun loadActions(channel: Channel?) {
        if (channel == null || type.value.isNullOrEmpty()) return
        loadActions(channel, type.value!!)
    }

    private fun loadActions(channels: List<Channel>) {
        if (channels.isNullOrEmpty()) return

        if (type.value == HoverAction.BALANCE)
            loadActions(channels, type.value!!)
    }

    private fun loadActions(channel: Channel, t: String) = viewModelScope.launch {
        channelActions.value = if (t == HoverAction.P2P) repo.getTransferActions(channel.id) else repo.getActions(channel.id, t)
    }

    private fun loadAccounts(channels: List<Channel>) = viewModelScope.launch {
        val ids = channels.map { it.id }
        accounts.value = repo.getAccounts(ids)
    }

    private fun loadActions(channels: List<Channel>, t: String) {
        val ids = channels.map { it.id }.toIntArray()

        viewModelScope.launch {
            channelActions.value = repo.getActions(ids, t)
        }
    }

    fun setChannelsSelected(channels: List<Channel>?) {
        viewModelScope.launch {
            if (channels.isNullOrEmpty()) return@launch

            channels.forEachIndexed { index, channel ->
                logChoice(channel)
                channel.selected = true
                channel.defaultAccount = selectedChannels.value.isNullOrEmpty() && index == 0
                repo.update(channel)
            }
        }
    }

    private fun logChoice(channel: Channel) {
        joinChannelGroup(channel.id, application.applicationContext)
        val args = JSONObject()

        try {
            args.put(application.getString(R.string.added_channel_id), channel.id)
        } catch (ignored: Exception) {
        }

        AnalyticsUtil.logAnalyticsEvent(application.getString(R.string.new_channel_selected), args, application.baseContext)
    }

    fun errorCheck(): String? {
        return when {
            activeChannel.value == null || activeAccount.value == null -> application.getString(R.string.channels_error_noselect)
            channelActions.value.isNullOrEmpty() -> application.getString(R.string.no_actions_fielderror,
                    HoverAction.getHumanFriendlyType(application, type.value))
            else -> null
        }
    }

    fun setChannelFromRequest(r: Request?) {
        if (r != null && !selectedChannels.value.isNullOrEmpty()) {
            viewModelScope.launch {
                var actions = repo.getActions(getChannelIds(selectedChannels.value!!), r.requester_institution_id)

                if (actions.isEmpty() && !simChannels.value.isNullOrEmpty())
                    actions = repo.getActions(getChannelIds(simChannels.value!!), r.requester_institution_id)

                if (actions.isNotEmpty())
                    channelActions.postValue(actions)
            }

            activeChannel.addSource(channelActions, this::setActiveChannel)
        }
    }

    private fun getChannelIds(channels: List<Channel>?): IntArray? = channels?.map { it.id }?.toIntArray()

    fun view(s: Schedule) {
        setType(s.type)
        setActiveChannel(repo.getChannel(s.channel_id)!!)
    }

    fun getFetchAccountAction(channelId: Int): HoverAction? = repo.getActions(channelId, HoverAction.FETCH_ACCOUNTS).firstOrNull()

    fun createAccounts(channels: List<Channel>) = viewModelScope.launch(Dispatchers.IO) {
        val defaultAccount = repo.getDefaultAccount()

        channels.forEach {
            if (getFetchAccountAction(it.id) == null) {
                with(it) {
                    val account = Account(name, name, logoUrl, accountNo, id, primaryColorHex, secondaryColorHex, defaultAccount == null)
                    repo.insert(account)
                }
            }
        }
    }

    fun migrateAccounts() = viewModelScope.launch(Dispatchers.IO) {
        if (accounts.value.isNullOrEmpty() && !selectedChannels.value.isNullOrEmpty()) {
            createAccounts(selectedChannels.value!!)
        }
    }

    fun fetchAccounts(channelId: Int) = viewModelScope.launch(Dispatchers.IO) {
        val channelAccounts = repo.getAccounts(channelId)
        accounts.postValue(channelAccounts)
    }

    private fun updateAccounts() = viewModelScope.launch(Dispatchers.IO) {
        val savedAccounts = repo.getAllAccounts()
        if (savedAccounts.isNullOrEmpty()) return@launch

        if (savedAccounts.none { it.isDefault }) {
            val defaultChannel: Channel? = selectedChannels.value?.firstOrNull { it.defaultAccount }

            if (defaultChannel != null) {
                val ids = savedAccounts.filter { it.channelId == defaultChannel.id }.map { it.id }.toList()
                ids.minOrNull()?.let { id -> repo.update(savedAccounts.first { it.id == id }) }
            }
        } else {
            Timber.e("Nothing to update. Default account set")
        }
    }

    override fun highlightAccount(account: Account) {
        viewModelScope.launch(Dispatchers.IO) {
            val channel = repo.getChannel(account.channelId)
            setActiveChannel(channel!!)
            activeAccount.postValue(account)
        }
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