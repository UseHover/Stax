package com.hover.stax.domain.use_case.sims

import com.hover.sdk.sims.SimInfo
import com.hover.stax.data.local.SimRepo
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.repository.AccountRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber

data class SimWithAccount(
    val sim: SimInfo,
    val account: Account?
)

class ListSimsUseCase(
    private val simRepo: SimRepo,
    private val accountRepository: AccountRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default) {

    suspend operator fun invoke(): List<SimWithAccount> =
        withContext(defaultDispatcher) {
            val sims = simRepo.getAll()
            Timber.e("found ${sims.size} sims. Loading accounts")
            val result: MutableList<SimWithAccount> = mutableListOf()
            for (sim in sims) {
                var account = accountRepository.getAccountBySim(sim.subscriptionId)
                Timber.e("loaded ${account?.name} account")
                if (account == null)
                    account = accountRepository.createAccount(sim)
                result.add(SimWithAccount(sim, account))
            }
            result
        }
}