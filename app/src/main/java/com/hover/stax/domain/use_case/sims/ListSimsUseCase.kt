package com.hover.stax.domain.use_case.sims

import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.sims.SimInfo
import com.hover.stax.data.local.SimRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.repository.AccountRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber

data class SimWithAccount(
    val sim: SimInfo,
    val account: Account,
    val balanceAction: HoverAction?,
    val airtimeActions: List<HoverAction>?
)

class ListSimsUseCase(
    private val simRepo: SimRepo,
    private val accountRepository: AccountRepository,
    private val actionRepository: ActionRepo,
    private val defaultDispatcher: CoroutineDispatcher) {

    suspend operator fun invoke(): List<SimWithAccount> = withContext(defaultDispatcher) {
        val sims = simRepo.getAll()
        val result: MutableList<SimWithAccount> = mutableListOf()
        for (sim in sims) {
            var account = accountRepository.getAccountBySim(sim.subscriptionId)
            var balanceAct: HoverAction? = null
            var airtimeActs: List<HoverAction>? = null

            if (account == null)
                account = accountRepository.createAccount(sim)

            if (account.channelId != -1) {
                balanceAct = actionRepository.getFirstAction(account.channelId, HoverAction.BALANCE)
                airtimeActs = actionRepository.getActionsByRecipientInstitution(account.institutionId!!, account.countryAlpha2!!, HoverAction.AIRTIME)
            }
            result.add(SimWithAccount(sim, account, balanceAct, airtimeActs))
        }
        result
    }
}