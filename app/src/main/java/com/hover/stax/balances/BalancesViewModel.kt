package com.hover.stax.balances

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.account.Account
import com.hover.stax.channels.Channel
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


class BalancesViewModel(val application: Application, val repo: DatabaseRepo) : ViewModel() {

    private var listener: RunBalanceListener? = null
    private val hasRunList = ArrayList<Int>()
    private var hasActive: Boolean = false

    var selectedChannels: LiveData<List<Channel>> = MutableLiveData()
    var accounts: LiveData<List<Account>> = MutableLiveData()

    var runFlag = MutableLiveData(NONE)
    var toRun = MediatorLiveData<List<Pair<Account?, HoverAction>>>()
    var actions: LiveData<List<HoverAction>> = MediatorLiveData()

    private var runBalanceError = MutableLiveData<Boolean>()

    init {
        selectedChannels = repo.selected
        accounts = repo.allAccounts

        actions = Transformations.switchMap(selectedChannels, this::loadActions)

        toRun.apply {
            value = ArrayList()
            addSource(runFlag, ::onSetRunning)
            addSource(actions, ::onActionsLoaded)
        }

        runBalanceError.value = false
    }

    fun setListener(l: RunBalanceListener) {
        listener = l
    }

    private fun loadActions(channels: List<Channel>): LiveData<List<HoverAction>> {
        val ids = channels.map { it.id }.toIntArray()
        return repo.getLiveActions(ids, listOf(HoverAction.FETCH_ACCOUNTS, HoverAction.BALANCE))
    }

    fun getChannel(id: Int): Channel? {
        val allChannels = selectedChannels.value ?: ArrayList()
        return getChannel(allChannels, id)
    }

    fun getChannel(channels: List<Channel>, id: Int): Channel? = channels.firstOrNull { it.id == id }

    fun setRunning(accountId: Int) {
        runFlag.value = accountId
    }

    fun setRunning(channel: Channel) {
        viewModelScope.launch(Dispatchers.IO) {
            val accounts = repo.getAccounts(channel.id)
            runFlag.postValue(accounts.firstOrNull()?.id ?: channel.id)
        }
    }

    fun setAllRunning(c: Context) {
        Utils.logAnalyticsEvent(c.getString(R.string.refresh_balance_all), c)
        runFlag.value = ALL
    }

    private fun onSetRunning(flag: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            when (flag) {
                NONE, null -> toRun.postValue(ArrayList())
                ALL -> startRun(getAccountActions(actions.value!!))
                else -> startRun(listOf(getAccountActions(flag)))
            }
        }
    }

    private fun onActionsLoaded(actions: List<HoverAction>) {
        viewModelScope.launch(Dispatchers.IO) {
            when {
                runFlag.value == null || toRun.value!!.isNotEmpty() -> {
                }
                runFlag.value == ALL -> startRun(getAccountActions(actions))
                runFlag.value != NONE -> startRun(listOf(getAccountActions(runFlag.value!!)))
            }
        }
    }

    private fun updateActionsIfRequired(actions: List<HoverAction>): List<HoverAction> {
        val actionList = ArrayList(actions)

        val channelIds = actionList.distinctBy { it.id }.filter { it.transaction_type == HoverAction.FETCH_ACCOUNTS }.map { it.channel_id }.toList()

        channelIds.forEach { id ->
            val actionToFilter = if (repo.getAccounts(id).isEmpty()) {
                actionList.first { it.channel_id == id && it.transaction_type == HoverAction.BALANCE }
            } else {
                actionList.first { it.channel_id == id && it.transaction_type == HoverAction.FETCH_ACCOUNTS }
            }

            actionList.remove(actionToFilter)
        }

        return actionList
    }

    private fun startRun(actionPairs: List<Pair<Account?, HoverAction>>) {
        if (!actionPairs.isNullOrEmpty()) {
            toRun.postValue(actionPairs)
            runNext(actionPairs, 0)
        }
    }

    private fun runNext(actionPairs: List<Pair<Account?, HoverAction>>, index: Int) {
        if (listener != null && !hasActive) {
            hasActive = true
            listener?.startRun(actionPairs[index], index)
        } else if (!hasActive) {
            UIHelper.flashMessage(application, "Failed to start run, please try again")
        }
    }

    fun setRan(index: Int) {
        var i = index

        hasActive = false

        if (toRun.value!!.size > i + 1) {
            hasRunList.add(toRun.value!![i].second.id)

            while (hasRunList.contains(toRun.value!![i + 1].second.id))
                i += 1

            if (toRun.value!!.size > i + 1)
                runNext(toRun.value!!, i + 1)
            else
                endRun()
        } else
            endRun()
    }

    private fun endRun() {
        toRun.value = ArrayList()
        runFlag.value = NONE
        hasRunList.clear()
    }
    
    private fun getAccountActions(flag: Int): Pair<Account?, HoverAction> {
        val account = repo.getAccount(flag)

        val actionsToRun = if (account == null)
            updateActionsIfRequired(actions.value!!.filter { it.channel_id == flag })
        else
            updateActionsIfRequired(actions.value!!.filter { it.channel_id == account.channelId })

        Timber.e("Action ${actionsToRun.first().transaction_type} - Inst ${actionsToRun.first().from_institution_name}")

        return Pair(account, actionsToRun.first())
    }

    private fun getAccountActions(actions: List<HoverAction>): List<Pair<Account, HoverAction>> {
        val updatedActions = updateActionsIfRequired(actions)

        return if (!accounts.value.isNullOrEmpty())
            return accounts.value!!.map { account -> Pair(account, updatedActions.first { it.channel_id == it.channel_id }) }
        else
            emptyList()
    }

    interface RunBalanceListener {
        fun startRun(actionPair: Pair<Account?, HoverAction>, index: Int)
    }

    companion object {
        const val ALL = -1
        const val NONE = 0
    }
}