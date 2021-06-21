package com.hover.stax.balances

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils


class BalancesViewModel(val application: Application, val repo: DatabaseRepo) : ViewModel() {

    private var listener: RunBalanceListener? = null
    private val hasRunList = ArrayList<Int>()
    private var hasActive: Boolean = false

    private var selectedChannels: LiveData<List<Channel>> = MutableLiveData()
    private var activeChannel = MutableLiveData<Channel>()
    private var actions: LiveData<List<HoverAction>> = MediatorLiveData()
    private var runFlag = MutableLiveData(NONE)
    private var toRun = MediatorLiveData<List<HoverAction>>()
    private var runBalanceError = MutableLiveData<Boolean>()

    init {
        selectedChannels = repo.selected
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

    fun getToRun(): LiveData<List<HoverAction>> = toRun

    fun getRunFlag(): LiveData<Int> = runFlag

    fun getSelectedChannels(): LiveData<List<Channel>> = selectedChannels

    fun loadActions(channelList: List<Channel>): LiveData<List<HoverAction>> {
        val ids = IntArray(channelList.size)
        for (c in channelList.indices) ids[c] = channelList[c].id
        return repo.getLiveActions(ids, HoverAction.BALANCE)
    }

    fun getActions(): LiveData<List<HoverAction>> = actions

    fun getChannel(id: Int): Channel? {
        val allChannels = selectedChannels.value ?: ArrayList()
        return getChannel(allChannels, id)
    }

    fun getChannel(channels: List<Channel>, id: Int): Channel? {
        channels.forEach { if (it.id == id) return it }
        return null
    }

    fun setRunning(channelId: Int) {
        runFlag.value = channelId
    }

    fun setAllRunning(c: Context) {
        Utils.logAnalyticsEvent(c.getString(R.string.refresh_balance_all), c)
        runFlag.value = ALL
    }

    fun onSetRunning(flag: Int?) {
        when (flag) {
            NONE, null -> toRun.value = ArrayList()
            ALL -> startRun(actions.value!!)
            else -> startRun(getChannelActions(flag))
        }
    }

    fun onActionsLoaded(actions: List<HoverAction>) {
        when {
            runFlag.value == null || toRun.value!!.isEmpty() -> return
            runFlag.value == ALL -> startRun(actions)
            runFlag.value != NONE -> startRun(getChannelActions(runFlag.value!!))
        }
    }

    fun startRun(actions: List<HoverAction>){
        if(!actions.isNullOrEmpty()) {
            toRun.value = actions
            runNext(actions, 0)
        }
    }

    fun runNext(actions: List<HoverAction>, index: Int) {
        if(listener != null && !hasActive){
            hasActive = true
            listener?.startRun(actions[index], index)
        } else if(!hasActive) {
            UIHelper.flashMessage(application, "Failed to start run, please try again")
        }
    }

    fun setRan(index: Int){
        var i = index

        hasActive = false

        if(toRun.value!!.size > i + 1){
            hasRunList.add(toRun.value!![i].id)

            while(hasRunList.contains(toRun.value!![i + 1].id))
                i += 1

            if(toRun.value!!.size > i + 1)
                runNext(toRun.value!!, i + 1)
            else
                endRun()
        } else
            endRun()
    }

    fun endRun(){
        toRun.value = ArrayList()
        runFlag.value = NONE
        hasRunList.clear()
    }

    fun getChannelActions(flag: Int): List<HoverAction> {
        val list = ArrayList<HoverAction>()

        if(actions.value.isNullOrEmpty()) return list

        actions.value!!.forEach {
            if(it.channel_id == flag)
                list.add(it)
        }

        return list
    }

    fun hasChannels(): Boolean = !selectedChannels.value.isNullOrEmpty()

    interface RunBalanceListener {
        fun startRun(a: HoverAction, index: Int)
    }

    companion object {
        const val ALL = -1
        const val NONE = 0
    }
}