/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.data.repository

import android.content.Context
import com.hover.sdk.api.ActionApi
import com.hover.sdk.sims.SimInfo
import com.hover.stax.R
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.data.local.channels.ChannelRepo
import com.hover.stax.domain.model.USSDAccount
import com.hover.stax.domain.repository.AccountRepository
import com.hover.stax.notifications.PushNotificationTopicsInterface
import com.hover.stax.utils.AnalyticsUtil
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AccountRepositoryImpl(
    val accountRepo: AccountRepo,
    private val channelRepo: ChannelRepo,
    val actionRepo: ActionRepo
) :
    AccountRepository, PushNotificationTopicsInterface, KoinComponent {

    private val context: Context by inject()

    override suspend fun createAccount(sim: SimInfo): USSDAccount {
        var account = USSDAccount(generateSimBasedName(sim), generateSimBasedAlias(sim))
        channelRepo.getTelecom(sim.osReportedHni)?.let {
            account = USSDAccount(account.userAlias, it, false, sim.subscriptionId)
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

    override suspend fun getAccountBySim(subscriptionId: Int): USSDAccount? {
        return accountRepo.getAccountBySim(subscriptionId)
    }

    private fun logChoice(account: USSDAccount) {
        joinChannelGroup(account.channelId, context)
        val args = JSONObject()

        try {
            args.put(context.getString(R.string.added_channel_id), account.channelId)
        } catch (ignored: Exception) {
        }

        AnalyticsUtil.logAnalyticsEvent(context.getString(R.string.new_sim_channel), args, context)
    }
}