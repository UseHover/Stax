package com.hover.stax.domain.use_case.accounts

import com.hover.stax.channels.Channel
import com.hover.stax.domain.repository.AccountRepository

class CreateAccountsUseCase(private val accountsRepository: AccountRepository) {

    suspend operator fun invoke(channels: List<Channel>): List<Long> {
        return accountsRepository.createAccounts(channels)
    }
}