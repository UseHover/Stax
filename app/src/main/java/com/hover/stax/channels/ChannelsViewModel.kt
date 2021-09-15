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
import com.hover.stax.account.Account
import com.hover.stax.account.AccountDropdown

import com.hover.stax.database.DatabaseRepo
import com.hover.stax.pushNotification.PushNotificationTopicsInterface
import com.hover.stax.requests.Request
import com.hover.stax.schedules.Schedule
import com.hover.stax.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber


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

    private val localBroadcastManager: LocalBroadcastManager = LocalBroadcastManager.getInstance(application)

    private val simReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModelScope.launch {
                sims.postValue(repo.presentSims)
            }
        }
    }

    init {
        type.value = HoverAction.BALANCE

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
        allChannels = repo.allChannels
        selectedChannels = repo.selected
    }

    private fun loadSims() {
        viewModelScope.launch {
            sims.postValue(repo.presentSims)
        }

        localBroadcastManager.registerReceiver(simReceiver, IntentFilter(Utils.getPackage(application).plus(".NEW_SIM_INFO_ACTION")))
        Hover.updateSimInfo(application)
    }

    private fun getHnisAndSubscribeToFirebase(sims: List<SimInfo>?): List<String>? {
        if (sims == null) return null

        val hniList = ArrayList<String>()

        for (sim in sims) {
            if (!hniList.contains(sim.osReportedHni)) {
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
        if (channels == null || hniList == null) return

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

    private fun setActiveChannelIfNull(channels: List<Channel>) {
        Timber.e("Setting active channel ${channels.firstOrNull()}")
        if (!channels.isNullOrEmpty() && activeChannel.value == null)
            activeChannel.value = channels.first { it.defaultAccount }
    }

    private fun setActiveChannel(channel: Channel) {
        activeChannel.postValue(channel)
    }

    //TODO make another function to set active account from channel actions
    private fun setActiveChannel(actions: List<HoverAction>) {
        if (actions.isNullOrEmpty()) return

        activeChannel.removeSource(channelActions)

        viewModelScope.launch {
            activeChannel.postValue(repo.getChannel(actions.first().channel_id))
        }
    }

    override fun highlightChannel(c: Channel?) {
        c?.let { setActiveChannel(it) }
    }

    private fun loadActions(t: String) {
        if ((t == HoverAction.BALANCE && selectedChannels.value == null) || (t != HoverAction.BALANCE && activeChannel.value == null)) return
        if (t == HoverAction.BALANCE) loadActions(selectedChannels.value!!, t) else loadActions(activeChannel.value!!, t)
    }

    private fun loadActions(channel: Channel) {
        loadActions(channel, type.value!!)
    }

    private fun loadActions(channels: List<Channel>) {
        if (type.value == HoverAction.BALANCE)
            loadActions(channels, type.value!!)
    }

    private fun loadActions(channel: Channel, t: String) {
        viewModelScope.launch {
            channelActions.value = if (t == HoverAction.P2P) repo.getTransferActions(channel.id) else repo.getActions(channel.id, t)
        }
    }

    private fun loadAccounts(channels: List<Channel>) {
        viewModelScope.launch {
            val ids = channels.map { it.id }
            accounts.value = repo.getAccounts(ids)
        }
    }

    private fun loadActions(channels: List<Channel>, t: String) {
        val ids = IntArray(channels.size)

        for (i in channels.indices)
            ids[i] = channels[i].id

        viewModelScope.launch {
            channelActions.value = repo.getActions(ids, t)
        }
    }

    fun saveChannels(channels: List<Channel>) {
        viewModelScope.launch {
            val toSkip = repo.getActions(getChannelIds(channels), HoverAction.FETCH_ACCOUNTS).map { it.channel_id }

            val channelsToAdd = channels.filterNot { toSkip.contains(it.id) }
            setChannelsSelected(channelsToAdd)
        }
    }

    fun setChannelsSelected(channels: List<Channel>?) {
        if (channels.isNullOrEmpty()) return

        channels.forEachIndexed { index, channel ->
            logChoice(channel)
            channel.selected = true
            channel.defaultAccount = selectedChannels.value.isNullOrEmpty() && index == 0
            repo.update(channel)
        }
    }

    private fun logChoice(channel: Channel) {
        joinChannelGroup(channel.id, application.applicationContext)
        val args = JSONObject()

        try {
            args.put(application.getString(R.string.added_channel_id), channel.id)
        } catch (ignored: Exception) {
        }

        Utils.logAnalyticsEvent(application.getString(R.string.new_channel_selected), args, application.baseContext)
    }

    fun errorCheck(): String? {
        return when {
            activeChannel.value == null -> application.getString(R.string.channels_error_noselect)
            channelActions.value.isNullOrEmpty() -> application.getString(R.string.no_actions_fielderror,
                    HoverAction.getHumanFriendlyType(application, type.value))
            else -> null
        }
    }

    fun setChannelFromRequest(r: Request?) {
        if (r != null && !selectedChannels.value.isNullOrEmpty()) {
            viewModelScope.launch {
                var actions = repo.getActions(getChannelIds(selectedChannels.value!!), r.requester_institution_id)
                if (actions.isEmpty())
                    actions = repo.getActions(getChannelIds(simChannels.value!!), r.requester_institution_id)

                if (actions.isNotEmpty())
                    channelActions.postValue(actions)
            }

            activeChannel.addSource(channelActions, this::setActiveChannel)
        }
    }

    private fun getChannelIds(channels: List<Channel>): IntArray = channels.map { it.id }.toIntArray()

    fun view(s: Schedule) {
        setType(s.type)
        setActiveChannel(repo.getChannel(s.channel_id))
    }

    fun getFetchAccountAction(channelId: Int): HoverAction? = repo.getActions(channelId, HoverAction.FETCH_ACCOUNTS).firstOrNull()

    fun createAccounts(channels: List<Channel>) {
        viewModelScope.launch {
            channels.forEach {
                if (getFetchAccountAction(it.id) == null)
                    repo.createAccount(it)
            }
        }
    }

    fun getAccounts(channelId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            accounts.postValue(repo.getAccounts(channelId))
        }
    }

    override fun onCleared() {
        try {
            localBroadcastManager.unregisterReceiver(simReceiver)
        } catch (ignored: Exception) {
        }

        super.onCleared()
    }

    override fun highlightAccount(account: Account) {
        viewModelScope.launch(Dispatchers.IO) {
            val channel = repo.getChannel(account.channelId)
            setActiveChannel(channel)
        }
    }
}