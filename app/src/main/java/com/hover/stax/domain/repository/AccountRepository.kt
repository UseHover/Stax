package com.hover.stax.domain.repository

import com.hover.sdk.sims.SimInfo
import com.hover.stax.domain.model.Account
import com.hover.stax.channels.Channel
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    val addedAccounts: Flow<List<Account>>

    suspend fun createAccount(sim: SimInfo): Account

    suspend fun getAccountBySim(subscriptionId: Int): Account?
}