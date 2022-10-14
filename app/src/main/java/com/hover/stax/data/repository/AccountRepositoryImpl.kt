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

class AccountRepositoryImpl(val accountRepo: AccountRepo, private val channelRepo: ChannelRepo, val actionRepo: ActionRepo) :
    AccountRepository, PushNotificationTopicsInterface, KoinComponent {

    private val context: Context by inject()

    override val fetchAccounts: Flow<List<Account>>
        get() = accountRepo.getAccounts()

    override suspend fun createAccount(sim: SimInfo): Account {
        var account = Account(generateSimBasedName(sim), generateSimBasedAlias(sim))
        channelRepo.getTelecom(sim.osReportedHni)?.let {
            account = Account(account.name, it, false, sim.subscriptionId)
            accountRepo.insert(account)
        }
        logChoice(account)
        ActionApi.scheduleActionConfigUpdate(account.countryAlpha2, 24, context)
        return account
    }

    private fun generateSimBasedName(sim: SimInfo): String {
        return (sim.operatorName ?: sim.networkOperatorName ?: "") + "-" + sim.subscriptionId.toString()
    }

    private fun generateSimBasedAlias(sim: SimInfo): String {
        return sim.operatorName ?: sim.networkOperatorName ?: "Unknown"
    }

    override suspend fun getAccountBySim(subscriptionId: Int): Account? {
        return accountRepo.getAccountBySim(subscriptionId)
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