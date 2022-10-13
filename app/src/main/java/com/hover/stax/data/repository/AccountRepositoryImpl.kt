package com.hover.stax.data.repository

import android.content.Context
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.ActionApi
import com.hover.sdk.sims.SimInfo
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.data.local.channels.ChannelRepo
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.model.PLACEHOLDER
import com.hover.stax.domain.repository.AccountRepository
import com.hover.stax.notifications.PushNotificationTopicsInterface
import com.hover.stax.utils.AnalyticsUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class AccountRepositoryImpl(val accountRepo: AccountRepo, val channelRepo: ChannelRepo, val actionRepo: ActionRepo, private val coroutineDispatcher: CoroutineDispatcher) : AccountRepository, PushNotificationTopicsInterface, KoinComponent {

    private val context: Context by inject()

    override val fetchAccounts: Flow<List<Account>>
        get() = accountRepo.getAccounts()

    override suspend fun createAccounts(channels: List<Channel>) {
        val defaultAccount = accountRepo.getDefaultAccountAsync()
        channels.mapIndexed { index, channel ->
            createAccount(channel, -1, defaultAccount == null && index == 0)
        }
    }

    override suspend fun createAccount(channel: Channel, simSubscriptionId: Int, isDefault: Boolean) {
        val accountName: String = if (getFetchAccountAction(channel.id) == null) channel.name else PLACEHOLDER //placeholder alias for easier identification later
        val account = Account(accountName, channel, isDefault, simSubscriptionId)

        channelRepo.update(channel)
        accountRepo.insert(account)

        logChoice(account)
        ActionApi.scheduleActionConfigUpdate(account.countryAlpha2, 24, context)
    }

    override suspend fun setDefaultAccount(account: Account) {
        fetchAccounts.collect { accounts ->
            val current = accounts.firstOrNull { it.isDefault }?.also {
                it.isDefault = false
            }

            val defaultAccount = accounts.first { it.id == account.id }.also { it.isDefault = true }

            withContext(coroutineDispatcher) {
                launch {
                    accountRepo.update(listOf(current!!, defaultAccount))
                }
            }
        }
    }

    override suspend fun createTelecomAccounts(sims: List<SimInfo>) {
        val telecomChannels = channelRepo.publishedTelecomChannels()
        Timber.i("all published Telecom channels in  is: ${telecomChannels.size}")
        sims.forEach { sim ->
            val channel = telecomChannels.firstOrNull { it.hniList.contains(sim.osReportedHni) }
            channel?.let {
                Timber.i("Found telecom channel: ${it.name} having country code: ${it.countryAlpha2}")
                createAccount(it, sim.subscriptionId, false)
            }
        }
    }

    override fun getTelecomAccounts(simSubscriptionIds: IntArray): Flow<List<Account>> {
        return accountRepo.
        getTelecomAccounts(simSubscriptionIds)
    }

    private fun getFetchAccountAction(channelId: Int): HoverAction? = actionRepo.getActions(channelId, HoverAction.FETCH_ACCOUNTS).firstOrNull()

    private fun logChoice(account: Account) {
        joinChannelGroup(account.channelId, context)
        val args = JSONObject()

        try {
            args.put(context.getString(R.string.added_channel_id), account.channelId)
        } catch (ignored: Exception) {
        }

        AnalyticsUtil.logAnalyticsEvent(context.getString(R.string.new_sim_channel), args, context)
    }
}