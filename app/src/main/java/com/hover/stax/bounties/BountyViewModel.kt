package com.hover.stax.bounties

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.auth.FirebaseUser
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.sims.SimInfo
import com.hover.stax.channels.Channel
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.transactions.StaxTransaction
import com.hover.stax.utils.Utils.getPackage
import kotlinx.coroutines.*
import org.koin.java.KoinJavaComponent.get
import java.util.*

private const val MAX_LOOKUP_COUNT = 40

class BountyViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = get(DatabaseRepo::class.java)

    @JvmField
    var country: String = CountryAdapter.codeRepresentingAllCountries()

    val actions: LiveData<List<HoverAction>>
    val channels: LiveData<List<Channel>>
    val transactions: LiveData<List<StaxTransaction>>
    private val filteredBountyChannels: MutableLiveData<List<Channel>?>
    private val bountyList = MediatorLiveData<List<Bounty>>()

    var sims: MutableLiveData<List<SimInfo>> = MutableLiveData()
    val bountyEmailLiveData: MutableLiveData<Map<Int, String?>> = MutableLiveData()
    private lateinit var deferredBountyList: Deferred<MutableList<Bounty>>

    val user = MutableLiveData<FirebaseUser>()
    val didLoginFail = MutableLiveData(false)

    init {
        loadSims()
        filteredBountyChannels = MutableLiveData()
        filteredBountyChannels.value = null
        actions = repo.bountyActions
        channels = Transformations.switchMap(actions, this::loadChannels)
        transactions = repo.bountyTransactions!!
        bountyList.addSource(actions, this::makeBounties)
        bountyList.addSource(transactions, this::makeBountiesIfActions)
    }

    fun setUser(firebaseUser: FirebaseUser) {
        user.value = firebaseUser
    }

    fun setLoginFailed(failed: Boolean) {
        didLoginFail.value = failed
    }

    fun uploadBountyUser(email: String, optedIn: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = BountyEmailNetworking(getApplication()).uploadBountyUser(email, optedIn)
            bountyEmailLiveData.postValue(result)
        }
    }

    private fun loadSims() {
        viewModelScope.launch {
            sims.postValue(repo.presentSims)
        }

        LocalBroadcastManager.getInstance(getApplication())
                .registerReceiver(simReceiver, IntentFilter(getPackage(getApplication()) + ".NEW_SIM_INFO_ACTION"))
        Hover.updateSimInfo(getApplication())
    }

    private val simReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModelScope.launch(Dispatchers.IO) {
                sims.postValue(repo.presentSims)
            }
        }
    }

    fun isSimPresent(b: Bounty): Boolean {
        if (sims.value.isNullOrEmpty()) return false
        for (sim in sims.value!!) {
            for (i in 0 until b.action.hni_list.length()) if (b.action.hni_list.optString(i) == sim.osReportedHni) return true
        }
        return false
    }

    private fun loadChannels(actions: List<HoverAction>?): LiveData<List<Channel>> {
        if (actions == null) return MutableLiveData()
        val ids = getChannelIdArray(actions.distinctBy { it.id }).toList()

        val channelList = runBlocking {
            getChannelsAsync(ids).await()
        }

        return MutableLiveData(channelList)
    }

    private fun getChannelsAsync(ids:List<Int>): Deferred<List<Channel>> = viewModelScope.async(Dispatchers.IO) {
        val channels = ArrayList<Channel>()

        ids.chunked(MAX_LOOKUP_COUNT).forEach { idList ->
            val results = repo.getChannelsByIds(idList)
            channels.addAll(results)
        }

        channels
    }

    val bounties: LiveData<List<Bounty>>
        get() = bountyList

    fun filterChannels(countryCode: String): LiveData<List<Channel>>? {
        country = countryCode
        val actions = actions.value ?: return null

        return if (countryCode == CountryAdapter.codeRepresentingAllCountries())
            loadChannels(actions)
        else
            repo.getChannelsByCountry(getChannelIdArray(actions), countryCode)
    }

    private fun getChannelIdArray(actions: List<HoverAction>): IntArray {
        return actions.distinctBy { it.channel_id }.map { it.channel_id }.toIntArray()
    }

    private fun makeBountiesIfActions(transactions: List<StaxTransaction>?) {
        if (actions.value != null && transactions != null) makeBounties(actions.value, transactions)
    }

    private fun makeBounties(actions: List<HoverAction>?) {
        if (actions != null) makeBounties(actions, transactions.value)
    }

    private fun makeBounties(actions: List<HoverAction>?, transactions: List<StaxTransaction>?) {
        viewModelScope.launch(Dispatchers.Main) {
            bountyList.value = getBounties(actions, transactions)
        }
    }

    private suspend fun getBounties(actions: List<HoverAction>?, transactions: List<StaxTransaction>?): MutableList<Bounty> {
        coroutineScope {
            deferredBountyList = async(Dispatchers.IO) {
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
                return@async bounties
            }

        }
        return deferredBountyList.await()
    }
}