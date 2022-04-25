package com.hover.stax.channels

import android.app.Application
import android.content.BroadcastReceiver
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.AccountDropdown
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.requests.Request
import com.hover.stax.schedules.Schedule
import com.hover.stax.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ChannelsViewModel(val application: Application, val repo: DatabaseRepo) : ViewModel(),
    AccountDropdown.HighlightListener {

    private var type = MutableLiveData<String>()

    val allChannels: LiveData<List<Channel>> = repo.publishedChannels
    val selectedChannels: LiveData<List<Channel>> = repo.selected

    val activeChannel = MediatorLiveData<Channel?>()
    val channelActions = MediatorLiveData<List<HoverAction>>()
    val accounts = MutableLiveData<List<Account>>()
    val activeAccount = MutableLiveData<Account>()
    private var simReceiver: BroadcastReceiver? = null

    init {
        activeChannel.addSource(selectedChannels, this::setActiveChannelIfNull)

        channelActions.apply {
            addSource(type, this@ChannelsViewModel::loadActions)
            addSource(selectedChannels, this@ChannelsViewModel::loadActions)
            addSource(activeChannel, this@ChannelsViewModel::loadActions)
        }

        loadAccounts()
    }

    fun setType(t: String) {
        type.value = t
    }

    fun getActionType(): String = type.value!!

    private fun setActiveChannelIfNull(channels: List<Channel>) {
        if (!channels.isNullOrEmpty() && activeChannel.value == null)
            activeChannel.value = channels.firstOrNull { it.defaultAccount }
    }

    fun setActiveChannel(channel: Channel) {
        activeChannel.postValue(channel)
    }

    fun setActiveAccount(account: Account?) {
        activeAccount.postValue(account!!)
    }

    private fun setActiveChannel(actions: List<HoverAction>) {
        if (actions.isNullOrEmpty()) return

        val channelAccounts = repo.getChannelAndAccounts(actions.first().channel_id)
        channelAccounts?.let {
            activeChannel.postValue(it.channel)
            setActiveAccount(it.accounts.firstOrNull())
        }
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

    private fun loadActions(channel: Channel, t: String) = viewModelScope.launch(Dispatchers.IO) {
        channelActions.postValue(
            if (t == HoverAction.P2P) repo.getTransferActions(channel.id)
            else repo.getActions(channel.id, t))
    }

    fun loadAccounts() = viewModelScope.launch {
        repo.getAccounts().collect { accounts.postValue(it) }
    }

    private fun loadActions(channels: List<Channel>, t: String) {
        val ids = channels.map { it.id }.toIntArray()

        viewModelScope.launch {
            channelActions.value = repo.getActions(ids, t)
        }
    }

    fun errorCheck(): String? {
        return when {
            activeChannel.value == null || activeAccount.value == null -> application.getString(R.string.channels_error_noselect)
            channelActions.value.isNullOrEmpty() -> application.getString(
                R.string.no_actions_fielderror,
                HoverAction.getHumanFriendlyType(application, type.value)
            )
            else -> null
        }
    }

    fun isValidAccount(): Boolean = activeAccount.value!!.name != Constants.PLACEHOLDER

    fun setChannelFromRequest(r: Request?) {
        if (r != null && !selectedChannels.value.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                var actions = repo.getActions(getChannelIds(selectedChannels.value!!), r.requester_institution_id)

                if (actions.isEmpty() && !allChannels.value.isNullOrEmpty())
                    actions = repo.getActions(getChannelIds(allChannels.value!!), r.requester_institution_id)

                if (actions.isNotEmpty())
                    channelActions.postValue(actions)

                setActiveChannel(actions)
            }
        }
    }

    private fun getChannelIds(channels: List<Channel>?): IntArray? = channels?.map { it.id }?.toIntArray()

    fun view(s: Schedule) {
        setType(s.type)
        setActiveChannel(repo.getChannel(s.channel_id)!!)
    }

    fun getChannel(channelId: Int): Channel? = repo.getChannel(channelId)

    fun runChannelFilter(search: String) {
        TODO("Whered it go?")
    }

    fun getFetchAccountAction(channelId: Int): HoverAction? {
        return if (repo.getActions(channelId, HoverAction.FETCH_ACCOUNTS).isNullOrEmpty()) null
            else repo.getActions(channelId, HoverAction.FETCH_ACCOUNTS)[0]
    }

    fun fetchAccounts(channelId: Int) = viewModelScope.launch(Dispatchers.IO) {
        val channelAccounts = repo.getAccounts(channelId)
        accounts.postValue(channelAccounts)
    }

    override fun highlightAccount(account: Account) {
        viewModelScope.launch(Dispatchers.IO) {
            val channel = repo.getChannel(account.channelId)
            setActiveChannel(channel!!)
            activeAccount.postValue(account)
        }
    }

    fun setDefaultAccount(account: Account) {
        if (!accounts.value.isNullOrEmpty()) {
            val accts = accounts.value!!
            //remove current default account
            val current: Account? = accts.firstOrNull { it.isDefault }
            current?.isDefault = false
            repo.update(current)

            val a = accts.first { it.id == account.id }
            a.isDefault = true
            repo.update(a)
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