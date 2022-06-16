package com.hover.stax.domain.use_case.accounts

import com.hover.stax.accounts.Account
import com.hover.stax.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

class GetAccountsUseCase(private val accountsRepository: AccountRepository) {

    suspend operator fun invoke(): Flow<List<Account>> {
        return accountsRepository.fetchAccounts()
    }
}