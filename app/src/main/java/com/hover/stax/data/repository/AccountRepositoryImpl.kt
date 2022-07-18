package com.hover.stax.data.repository

import android.content.Context
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.ActionApi
import com.hover.sdk.sims.SimInfo
import com.hover.stax.R
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.channels.Channel
import com.hover.stax.data.local.channels.ChannelRepo
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.model.PLACEHOLDER
import com.hover.stax.domain.repository.AccountRepository
import com.hover.stax.domain.use_case.sims.GetPresentSimUseCase
import com.hover.stax.notifications.PushNotificationTopicsInterface
import com.hover.stax.utils.AnalyticsUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AccountRepositoryImpl(val accountRepo: AccountRepo, val channelRepo: ChannelRepo, private val presentSimUseCase: GetPresentSimUseCase,  val actionRepo: ActionRepo, private val coroutineDispatcher: CoroutineDispatcher) : AccountRepository, PushNotificationTopicsInterface, KoinComponent {

    private val context: Context by inject()

    override val fetchAccounts: Flow<List<Account>>
        get() = accountRepo.getAccounts()

    override suspend fun createAccounts(channels: List<Channel>): List<Long> {
        val defaultAccount = accountRepo.getDefaultAccountAsync()

        val accounts = channels.mapIndexed { index, channel ->
            val subscriptionId = getSubscriptionId(channel)
            val accountName: String = if (getFetchAccountAction(channel.id) == null) channel.name else PLACEHOLDER //placeholder alias for easier identification later
            Account(
                accountName, channel.name, channel.logoUrl, channel.accountNo, channel.id, channel.institutionType, channel.countryAlpha2,
                channel.id, channel.primaryColorHex, channel.secondaryColorHex, defaultAccount == null && index == 0, subscriptionId = subscriptionId
            )
        }.onEach {
            logChoice(it)
            ActionApi.scheduleActionConfigUpdate(it.countryAlpha2, 24, context)
        }

        channels.onEach { it.selected = true }.also { channelRepo.update(it) }
        return accountRepo.insert(accounts)
    }

    //This only gets ID if account is a Telecos e.g Safaricom, MTN. It assumes different Teleco for each sim slots.
    //For better accuracy, we need user to manually select the preferred SIM card due to the edge case of same 2 telecos on the same device.
    private suspend fun getSubscriptionId(channel : Channel) : Int? {
        var subscriptionId : Int? = null
        //if(channel.institutionType == Channel.TELECOM_TYPE) {
            val presentSims = presentSimUseCase()
            if(presentSims.isEmpty()) return null
            val simInfo : SimInfo? = presentSims.find { channel.hniList.contains(it.osReportedHni) }
            simInfo?.let {
                subscriptionId = it.subscriptionId
            }
        //}
        return subscriptionId
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

    private fun getFetchAccountAction(channelId: Int): HoverAction? = actionRepo.getActions(channelId, HoverAction.FETCH_ACCOUNTS).firstOrNull()

    private fun logChoice(account: Account) {
        joinChannelGroup(account.channelId, context)
        val args = JSONObject()

        try {
            args.put(context.getString(R.string.added_channel_id), account.channelId)
        } catch (ignored: Exception) {
        }

        AnalyticsUtil.logAnalyticsEvent(context.getString(R.string.new_channel_selected), args, context)
    }
}