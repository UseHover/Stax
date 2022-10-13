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
import com.hover.stax.domain.model.Account
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.data.local.bonus.BonusRepo
import com.hover.stax.channels.Channel
import com.hover.stax.data.local.channels.ChannelRepo
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.domain.model.PLACEHOLDER
import com.hover.stax.domain.use_case.sims.GetPresentSimUseCase
import com.hover.stax.notifications.PushNotificationTopicsInterface
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel as KChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

//TODO: Refactor this class, and note the comment in the createAccounts(channels: List<Channel>) method
//Todo created by Tobi, 11th of August, 2022.
class ChannelsViewModel(application: Application, val repo: ChannelRepo,
                        val presentSimUseCase: GetPresentSimUseCase,
                        val accountRepo: AccountRepo,
                        val actionRepo: ActionRepo,
                        private val bonusRepo: BonusRepo) : AndroidViewModel(application),
    PushNotificationTopicsInterface {

    val accounts: LiveData<List<Account>> = accountRepo.getAllLiveAccounts()
    val allChannels: LiveData<List<Channel>> = repo.publishedNonTelecomChannels

    var sims = MutableLiveData<List<SimInfo>>()
    var simCountryList: LiveData<List<String>> = MutableLiveData()

    var countryChoice: MediatorLiveData<String> = MediatorLiveData()
    val filterQuery = MutableLiveData<String?>()
    private var countryChannels = MediatorLiveData<List<Channel>>()
    val filteredChannels = MediatorLiveData<List<Channel>>()

    private val _channelCountryList = MediatorLiveData<List<String>>()
    val channelCountryList: LiveData<List<String>> = _channelCountryList

    private val accountCreatedEvent = MutableSharedFlow<Boolean>()
    val accountEventFlow = accountCreatedEvent.asSharedFlow()

    private val accountChannel = KChannel<Account>()
    val accountCallback = accountChannel.receiveAsFlow()

    private var simReceiver: BroadcastReceiver? = null

    init {
        setSimBroadcastReceiver()
        loadSims()

        simCountryList = Transformations.map(sims, this::getCountriesAndFirebaseSubscriptions)
        countryChoice.addSource(simCountryList, this@ChannelsViewModel::onSimUpdate)

        countryChannels.apply {
            addSource(allChannels, this@ChannelsViewModel::onAllChannelsUpdate)
            addSource(countryChoice, this@ChannelsViewModel::onChoiceUpdate)
        }

        _channelCountryList.addSource(allChannels, this::loadChannelCountryList)

        filteredChannels.addSource(filterQuery, this@ChannelsViewModel::search)
        filteredChannels.addSource(countryChannels, this@ChannelsViewModel::updateCountryChannels)
    }

    private fun loadSims() {
        viewModelScope.launch(Dispatchers.IO) { sims.postValue(presentSimUseCase.invoke()) }

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
                    sims.postValue(presentSimUseCase.invoke())
                }
            }
        }
    }

    private fun onAllChannelsUpdate(channels: List<Channel>?) {
        updateCountryChannels(channels, countryChoice.value)
    }

    private fun onChoiceUpdate(countryCode: String?) {
        updateCountryChannels(allChannels.value, countryCode)
    }

    private fun loadChannelCountryList(channels: List<Channel>) = viewModelScope.launch {
        val countryCodes = mutableListOf(CountryAdapter.CODE_ALL_COUNTRIES)
        countryCodes.addAll(channels.map { it.countryAlpha2 }.distinct().sorted())

        _channelCountryList.postValue(countryCodes)
    }

    private fun onSimUpdate(countryCodes: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (countryCodes.isNotEmpty()) {
                for (code in countryCodes) {
                    if (repo.getChannelsByCountry(code).isNotEmpty())
                        updateCountry(code)
                }
            }
        }
    }

    private fun updateCountryChannels(channels: List<Channel>?, countryCode: String?) {
        countryChannels.value = when {
            countryCode.isNullOrEmpty() || countryCode == CountryAdapter.CODE_ALL_COUNTRIES -> channels
            else -> channels?.filter { it.countryAlpha2 == countryChoice.value }
        }
    }

    private fun getCountriesAndFirebaseSubscriptions(sims: List<SimInfo>?): List<String>? {
        setFirebaseSubscriptions(sims)
        return sims?.map { it.countryIso }
    }

    private fun setFirebaseSubscriptions(sims: List<SimInfo>?) {
        if (sims == null) return

        val hniList = ArrayList<String>()

        for (sim in sims) {
            if (!hniList.contains(sim.osReportedHni) && sim.countryIso != null) {
                FirebaseMessaging.getInstance().subscribeToTopic("sim-".plus(sim.osReportedHni))
                FirebaseMessaging.getInstance().subscribeToTopic(sim.countryIso.uppercase())
                hniList.add(sim.osReportedHni)
            }
        }
    }

    private fun logChoice(account: Account) {
        joinChannelGroup(account.channelId, getApplication() as Context)
        val args = JSONObject()

        try {
            args.put((getApplication() as Context).getString(R.string.added_channel_id), account.channelId)
        } catch (ignored: Exception) {
        }

        AnalyticsUtil.logAnalyticsEvent((getApplication() as Context).getString(R.string.new_channel_selected), args, getApplication() as Context)
    }

    fun validateAccounts(channelId: Int) = viewModelScope.launch(Dispatchers.IO) {
        val accounts = accountRepo.getAccountsByChannel(channelId)

        if (accounts.isEmpty())
            createAccount(channelId)
        else
            accountCreatedEvent.emit(true)
    }

    private fun createAccount(channelId: Int) = viewModelScope.launch(Dispatchers.IO) {
        val channel = repo.getChannel(channelId)
        channel?.let { createAccounts(listOf(it)) }

        accountCreatedEvent.emit(true)
    }

    fun createAccounts(channels: List<Channel>) = viewModelScope.launch(Dispatchers.IO) {
        val defaultAccount = accountRepo.getDefaultAccount()

        val accounts = channels.mapIndexed { index, channel ->
            val accountName: String = if (getFetchAccountAction(channel.id) == null) channel.name else channel.name.plus(PLACEHOLDER) //ensures uniqueness of name due to db constraints
            Account(accountName, channel, defaultAccount == null && index == 0, -1)
        }.onEach {
            logChoice(it)
            ActionApi.scheduleActionConfigUpdate(it.countryAlpha2, 24, getApplication())
        }

        val accountIds = accountRepo.insert(accounts)

        //Refactoring tip: This is currently the only difference when compared with the function in AccountRepositoryImpl.
        promptBalanceCheck(accountIds.first().toInt())
    }

    private fun promptBalanceCheck(accountId: Int) = viewModelScope.launch(Dispatchers.IO) {
        val account = accountRepo.getAccount(accountId)

        account?.let {
            accountChannel.send(it)
        }
    }

    private fun getFetchAccountAction(channelId: Int): HoverAction? = actionRepo.getActions(channelId, HoverAction.FETCH_ACCOUNTS).firstOrNull()

    fun updateSearch(value: String) {
        filterQuery.value = value
    }

    private fun search(value: String?) {
        countryChannels.value?.let { runFilter(it, value) }
    }

    fun updateCountry(code: String) {
        countryChoice.postValue(code.uppercase())
    }

    private fun updateCountryChannels(channels: List<Channel>?) {
        channels?.let { runFilter(it, filterQuery.value) }
    }

    private fun runFilter(channels: List<Channel>, value: String?) {
        filterBonusChannels(channels.filter { standardizeString(it.toString()).contains(standardizeString(value)) })
    }

    private fun filterBonusChannels(channels: List<Channel>) = viewModelScope.launch {
        bonusRepo.bonuses.collect { list ->
            val ids = list.map { it.purchaseChannel }
            filteredChannels.value = if (ids.isEmpty())
                channels
            else
                channels.filterNot { ids.contains(it.id) }
        }
    }

    fun updateChannel(channel: Channel) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.update(channel)
        }
    }

    private fun standardizeString(value: String?): String {
        // a non null String always contains an empty string
        if (value == null) return ""
        return value.lowercase().replace(" ", "").replace("#", "").replace("-", "");
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