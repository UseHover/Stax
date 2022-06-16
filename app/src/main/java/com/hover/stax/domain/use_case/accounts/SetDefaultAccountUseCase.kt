package com.hover.stax.domain.use_case.accounts

import com.hover.stax.accounts.Account
import com.hover.stax.domain.repository.AccountRepository

class SetDefaultAccountUseCase(private val accountsRepository: AccountRepository) {

    suspend operator fun invoke(account: Account) {
        accountsRepository.setDefaultAccount(account)
    }
}