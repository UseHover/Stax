package com.hover.stax.bounties

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.sims.SimInfo
import com.hover.stax.actions.ActionRepo
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelRepo
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.transactions.StaxTransaction
import com.hover.stax.transactions.TransactionRepo
import com.hover.stax.utils.Utils.getPackage
import kotlinx.coroutines.*
import timber.log.Timber

private const val MAX_LOOKUP_COUNT = 40

class BountyViewModel(application: Application, val repo: ChannelRepo, val actionRepo: ActionRepo, transactionRepo: TransactionRepo) : AndroidViewModel(application) {

    @JvmField
    var country: String = CountryAdapter.CODE_ALL_COUNTRIES

    val actions: LiveData<List<HoverAction>>
    val channels: MediatorLiveData<List<Channel>> = MediatorLiveData()
    val transactions: LiveData<List<StaxTransaction>>
    private val bountyList = MediatorLiveData<List<Bounty>>()

    private val _bounties = MutableLiveData<List<ChannelBounties>>()
    val bounties: LiveData<List<ChannelBounties>> = _bounties

    private var _channelCountryList = MediatorLiveData<List<String>>()
    val channelCountryList: LiveData<List<String>> = _channelCountryList

    var sims: MutableLiveData<List<SimInfo>> = MutableLiveData()
    private lateinit var bountyListAsync: Deferred<MutableList<Bounty>>

    private var simReceiver: BroadcastReceiver? = null

    init {
        simReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                viewModelScope.launch {
                    sims.postValue(repo.presentSims)
                }
            }
        }

        loadSims()
        actions = actionRepo.bountyActions
        channels.addSource(actions, this::loadChannels)
        _channelCountryList.addSource(channels, this::loadCountryList)
        transactions = transactionRepo.bountyTransactions!!
        bountyList.apply {
            addSource(actions, this@BountyViewModel::makeBounties)
            addSource(transactions, this@BountyViewModel::makeBountiesIfActions)
        }
    }

    private fun loadSims() {
        viewModelScope.launch {
            sims.postValue(repo.presentSims)
        }

        simReceiver?.let {
            LocalBroadcastManager.getInstance(getApplication())
                .registerReceiver(it, IntentFilter(getPackage(getApplication()) + ".NEW_SIM_INFO_ACTION"))
        }
        Hover.updateSimInfo(getApplication())
    }

    fun isSimPresent(b: Bounty): Boolean {
        if (sims.value.isNullOrEmpty()) return false
        for (sim in sims.value!!) {
            for (i in 0 until b.action.hni_list.length()) if (b.action.hni_list.optString(i) == sim.osReportedHni) return true
        }
        return false
    }

    private fun loadChannels(actions: List<HoverAction>?) = viewModelScope.launch(Dispatchers.IO) {
        if (actions == null) return@launch
        Timber.e("Actions loaded, fetching channels")

        val ids = getChannelIdArray(actions.distinctBy { it.id }).toList()

        val channelList = getChannelsAsync(ids)
        channels.postValue(channelList)
        Timber.e("Found ${channelList.size} channels ")

        _bounties.postValue(filterBounties())
    }

    private fun loadCountryList(channels: List<Channel>) = viewModelScope.launch {
        val countryCodes = mutableListOf(country)
        countryCodes.addAll(channels.map { it.countryAlpha2 }.sorted().distinct())
        _channelCountryList.postValue(countryCodes)
    }

    private suspend fun getChannelsAsync(ids: List<Int>): List<Channel> {
        val list: Deferred<List<Channel>>

        coroutineScope {
            list = async(Dispatchers.IO) {
                val channels = ArrayList<Channel>()

                ids.chunked(MAX_LOOKUP_COUNT).forEach { idList ->
                    val results = repo.getChannelsByIds(idList)
                    channels.addAll(results)
                }

                channels
            }
        }

        return list.await()
    }

    fun filterChannels(countryCode: String) = viewModelScope.launch(Dispatchers.IO) {
        country = countryCode
        val actions = actions.value ?: return@launch

         if (countryCode == CountryAdapter.CODE_ALL_COUNTRIES)
            loadChannels(actions)
        else {
            val countryChannels = repo.getChannelsByCountryCode(getChannelIdArray(actions), countryCode)
             channels.postValue(countryChannels)
             Timber.e("Found ${countryChannels.size} channels for $countryCode")
             _bounties.postValue(filterBounties())
         }
    }

    private fun getChannelIdArray(actions: List<HoverAction>): IntArray = actions.distinctBy { it.channel_id }.map { it.channel_id }.toIntArray()

    private fun makeBountiesIfActions(transactions: List<StaxTransaction>?) {
        if (actions.value != null && transactions != null) makeBounties(actions.value, transactions)
    }

    private fun makeBounties(actions: List<HoverAction>?) {
        if (actions != null) {
            Timber.e("Actions loaded, now to fetch bounties.")
            makeBounties(actions, transactions.value)
        }
    }

    private fun makeBounties(actions: List<HoverAction>?, transactions: List<StaxTransaction>?) = viewModelScope.launch(Dispatchers.IO) {
        Timber.e("We are making bounties with ${actions?.size} actions and ${transactions?.size} transactions")
        val loadedBounties = getBounties(actions, transactions)
        bountyList.postValue(loadedBounties)

        Timber.e("While making bounties, we found ${loadedBounties.size} bounties.")

        val channelBounties = filterBounties()
        Timber.e("We made bounties and found ${channelBounties.size} channelBounties")
        _bounties.postValue(channelBounties)
    }

    private fun filterBounties(): List<ChannelBounties> {
        Timber.e("Filtering bounties")

        Timber.e("Channels are ${channels.value?.size} and bounties are ${bountyList.value?.size}")
        if (channels.value.isNullOrEmpty() || bountyList.value.isNullOrEmpty())
            return emptyList()

        val openBounties = bountyList.value!!.filter { it.action.bounty_is_open || it.transactionCount != 0 }
        Timber.e("Found ${openBounties.size} open bounties")

        val channelBounties = channels.value!!.filter { c ->
            openBounties.any { it.action.channel_id == c.id }
        }.map { channel ->
            ChannelBounties(channel, openBounties.filter { it.action.channel_id == channel.id })
        }

        Timber.e("Channel bounties fetched current country is ${channelBounties.size}")
        return channelBounties
    }

    private suspend fun getBounties(actions: List<HoverAction>?, transactions: List<StaxTransaction>?): List<Bounty> {
        coroutineScope {
            bountyListAsync = async(Dispatchers.IO) {
                val bounties: MutableList<Bounty> = ArrayList()
                val transactionsCopy: MutableList<StaxTransaction> = if (transactions == null) ArrayList() else ArrayList(transactions)
                for (action in actions!!) {
                    val filterTransactions: MutableList<StaxTransaction> = ArrayList()
                    val iter = transactionsCopy.listIterator()
                    while (iter.hasNext()) {
                        val t = iter.next()
                        if (t.action_id == action.public_id) {
                            filterTransactions.add(t)
                            iter.remove()
                        }
                    }
                    bounties.add(Bounty(action, filterTransactions))
                }
                bounties
            }
        }
        return bountyListAsync.await()
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