package com.hover.stax.domain.use_case.accounts

import com.hover.sdk.sims.SimInfo
import com.hover.stax.channels.Channel
import com.hover.stax.domain.repository.AccountRepository

class CreateAccountsUseCase(private val accountsRepository: AccountRepository) {

    suspend operator fun invoke(channels: List<Channel>) {
        return accountsRepository.createAccounts(channels)
    }

    suspend fun createTelecomAccounts(sims: List<SimInfo>) {
        return accountsRepository.createTelecomAccounts(sims)
    }
}