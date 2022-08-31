package com.hover.stax.domain.use_case.accounts

import com.hover.stax.domain.model.Account
import com.hover.stax.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

class GetAccountsUseCase(private val accountsRepository: AccountRepository) {

    val accounts: Flow<List<Account>> = accountsRepository.fetchAccounts

    fun telecomAccounts(simSubscriptionIds: IntArray) : Flow<List<Account>> = accountsRepository.getTelecomAccounts(simSubscriptionIds)

}