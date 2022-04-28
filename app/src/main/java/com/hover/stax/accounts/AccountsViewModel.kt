package com.hover.stax.accounts

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.actions.ActionRepo
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelRepo
import com.hover.stax.requests.Request
import com.hover.stax.schedules.Schedule
import com.hover.stax.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountsViewModel(application: Application, val repo: AccountRepo, val actionRepo: ActionRepo, val channelRepo: ChannelRepo) : AndroidViewModel(application),
    AccountDropdown.HighlightListener {

    private var type = MutableLiveData<String>()

    val accounts: LiveData<List<Account>> = repo.getAllLiveAccounts()
    val activeAccount: MediatorLiveData<Account> = MediatorLiveData()
    val activeAccountChannel: LiveData<Channel?>

    val channelActions = MediatorLiveData<List<HoverAction>>()

    val allChannels: LiveData<List<Channel>> = channelRepo.publishedChannels
    val selectedChannels: LiveData<List<Channel>> = channelRepo.selected

    private var simReceiver: BroadcastReceiver? = null

    init {
        activeAccount.addSource(accounts, this@AccountsViewModel::setActiveAccountIfNull)
        activeAccountChannel = Transformations.map(activeAccount) { it?.let { channelRepo.getChannel(it.channelId) } }

        channelActions.apply {
            addSource(type, this@AccountsViewModel::loadActions)
            addSource(activeAccount, this@AccountsViewModel::loadActions)
        }
    }

    fun setType(t: String) {
        type.value = t
    }

    private fun setActiveAccountIfNull(accounts: List<Account>) {
        if (!accounts.isNullOrEmpty() && activeAccount.value == null)
            activeAccount.postValue(accounts.firstOrNull { it.isDefault })
    }

    fun getActionType(): String = type.value!!

    private fun loadActions(type: String?) {
        if (type == null || activeAccount.value == null) return

        if (accounts.value.isNullOrEmpty()) return
        loadActions(activeAccount.value!!, type)
    }

    private fun loadActions(account: Account?) {
        if (account == null || type.value.isNullOrEmpty()) return
        loadActions(account, type.value!!)
    }

    private fun loadActions(account: Account, t: String) = viewModelScope.launch(Dispatchers.IO) {
        channelActions.postValue(
            if (t == HoverAction.P2P) actionRepo.getTransferActions(account.channelId)
            else actionRepo.getActions(account.channelId, t))
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

    private fun getChannelIds(accounts: List<Account>?): IntArray? = accounts?.map { it.channelId }?.toIntArray()

    fun view(s: Schedule) {
        setType(s.type)
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
            repo.update(current)

            val a = accts.first { it.id == account.id }
            a.isDefault = true
            repo.update(a)
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