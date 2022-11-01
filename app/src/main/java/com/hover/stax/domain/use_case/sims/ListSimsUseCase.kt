package com.hover.stax.domain.use_case.sims

import com.hover.sdk.actions.HoverAction
import com.hover.sdk.sims.SimInfo
import com.hover.stax.data.local.SimRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.model.Bonus
import com.hover.stax.domain.repository.AccountRepository
import com.hover.stax.domain.repository.BonusRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber

data class SimWithAccount(
    val sim: SimInfo,
    val account: Account,
    val balanceAction: HoverAction?,
    val airtimeAction: HoverAction?,
    val bonus: Double
)

class ListSimsUseCase(
    private val simRepo: SimRepo,
    private val accountRepository: AccountRepository,
    private val actionRepository: ActionRepo,
    private val bonusRepository: BonusRepository,
    private val defaultDispatcher: CoroutineDispatcher) {

    suspend operator fun invoke(): List<SimWithAccount> = withContext(defaultDispatcher) {
        val sims = simRepo.getAll()
        val result: MutableList<SimWithAccount> = mutableListOf()
        for (sim in sims) {
            var account = accountRepository.getAccountBySim(sim.subscriptionId)
            var balanceAct: HoverAction? = null
            var airtimeAct: HoverAction? = null
            val bonus : Double? = getSimBonus(bonusRepository.bonusList(), sim.osReportedHni)?.bonusPercent

            if (account == null)
                account = accountRepository.createAccount(sim)

            if (account.channelId != -1) {
                balanceAct = actionRepository.getFirstAction(account.channelId, HoverAction.BALANCE)
                airtimeAct = actionRepository.getFirstAction(account.channelId, HoverAction.AIRTIME)
            }
            result.add(SimWithAccount(sim, account, balanceAct, airtimeAct, (bonus ?: 0.toDouble())))
        }
        result
    }

    private fun getSimBonus(bonuses: List<Bonus>, simHni: String) : Bonus? {
        return bonuses.find { it.hniList.contains(simHni) }
    }
}