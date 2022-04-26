package com.hover.stax.channels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.AccountDropdown
import com.hover.stax.accounts.AccountRepo
import com.hover.stax.actions.ActionRepo
import com.hover.stax.requests.Request
import com.hover.stax.schedules.Schedule
import com.hover.stax.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChannelsViewModel(application: Application, val repo: ChannelRepo, val actionRepo: ActionRepo, val accountRepo: AccountRepo) : AndroidViewModel(application),
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
            addSource(activeAccount, this@ChannelsViewModel::loadActions)
        }

        viewModelScope.launch(Dispatchers.IO) {
            accounts.postValue(accountRepo.getAllAccounts())
        }
    }

    fun setType(t: String) {
        type.value = t
    }

    fun setActiveAccount(account: Account?) {
        activeAccount.postValue(account!!)
    }

    private fun setActiveChannelIfNull(channels: List<Channel>) {
        if (!channels.isNullOrEmpty() && activeChannel.value == null)
            activeChannel.value = channels.firstOrNull { it.defaultAccount }
        }

    fun setActiveChannel(channel: Channel) {
        activeChannel.postValue(channel)
    }

    fun getActionType(): String = type.value!!

    private fun setActiveChannel(actions: List<HoverAction>) {
        if (actions.isNullOrEmpty()) return

        val channelAccounts = repo.getChannelAndAccounts(actions.first().channel_id)
        channelAccounts?.let {
            activeChannel.postValue(it.channel)
            setActiveAccount(it.accounts.firstOrNull())
        }
    }

    private fun loadActions(type: String?) {
        if (type == null || activeAccount.value == null) return

        if (accounts.value.isNullOrEmpty()) return
        if (type == HoverAction.BALANCE)
            loadActions(selectedChannels.value!!, type)
        else
            loadActions(activeAccount.value!!, type)
    }

    private fun loadActions(account: Account?) {
        if (account == null || type.value.isNullOrEmpty()) return
        loadActions(account, type.value!!)
    }

    private fun loadActions(channels: List<Channel>) {
        if (channels.isNullOrEmpty()) return

        if (type.value == HoverAction.BALANCE)
            loadActions(channels, type.value!!)
    }

    private fun loadActions(account: Account, t: String) = viewModelScope.launch(Dispatchers.IO) {
        channelActions.postValue(
            if (t == HoverAction.P2P) actionRepo.getTransferActions(account.channelId)
            else actionRepo.getActions(account.channelId, t))
    }

    private fun loadActions(channels: List<Channel>, t: String) {
        val ids = channels.map { it.id }.toIntArray()

        viewModelScope.launch {
            channelActions.value = actionRepo.getActions(ids, t)
        }
    }

    fun errorCheck(): String? {
        return when {
            activeAccount.value == null -> (getApplication() as Context).getString(R.string.channels_error_noselect)
            channelActions.value.isNullOrEmpty() -> (getApplication() as Context).getString(
                R.string.no_actions_fielderror,
                HoverAction.getHumanFriendlyType(getApplication(), type.value)
            )
            else -> null
        }
    }

    fun isValidAccount(): Boolean = activeAccount.value!!.name != Constants.PLACEHOLDER

    fun setChannelFromRequest(r: Request?) {
        if (r != null && !selectedChannels.value.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                var actions = actionRepo.getActions(getChannelIds(selectedChannels.value!!), r.requester_institution_id)

                if (actions.isEmpty() && !allChannels.value.isNullOrEmpty())
                    actions = actionRepo.getActions(getChannelIds(allChannels.value!!), r.requester_institution_id)

                if (actions.isNotEmpty())
                    channelActions.postValue(actions)

                setActiveChannel(actions)
            }
        }
    }

    private fun getChannelIds(channels: List<Channel>?): IntArray? = channels?.map { it.id }?.toIntArray()

    fun view(s: Schedule) {
        setType(s.type)
    }

    fun getChannel(channelId: Int): Channel? = repo.getChannel(channelId)

    fun runChannelFilter(search: String) {
        TODO("Whered it go?")
    }

    fun getFetchAccountAction(channelId: Int): HoverAction? {
        return if (actionRepo.getActions(channelId, HoverAction.FETCH_ACCOUNTS).isNullOrEmpty()) null
            else actionRepo.getActions(channelId, HoverAction.FETCH_ACCOUNTS)[0]
    }

    fun fetchAccounts(channelId: Int) = viewModelScope.launch(Dispatchers.IO) {
        val channelAccounts = accountRepo.getAccounts(channelId)
        accounts.postValue(channelAccounts)
    }

    override fun highlightAccount(account: Account) {
        activeAccount.postValue(account)
    }

    fun setDefaultAccount(account: Account) {
        if (!accounts.value.isNullOrEmpty()) {
            val accts = accounts.value!!
            //remove current default account
            val current: Account? = accts.firstOrNull { it.isDefault }
            current?.isDefault = false
            accountRepo.update(current)

            val a = accts.first { it.id == account.id }
            a.isDefault = true
            accountRepo.update(a)
        }
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