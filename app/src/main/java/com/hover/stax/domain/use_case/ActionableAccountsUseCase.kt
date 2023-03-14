package com.hover.stax.domain.use_case

import com.hover.sdk.actions.HoverAction
import com.hover.sdk.sims.SimInfo
import com.hover.stax.data.local.SimRepo
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.model.USDCAccount
import com.hover.stax.domain.model.USSDAccount
import com.hover.stax.domain.model.USSD_TYPE
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber

data class ActionableAccount(
    val account: Account,
    val sim: SimInfo?,
    val ussdAccount: USSDAccount?,
    val usdcAccount: USDCAccount?,
    val actions: List<HoverAction>?
)

class ActionableAccountsUseCase(
    val accountRepo: AccountRepo,
    private val simRepo: SimRepo,
    private val actionRepository: ActionRepo,
    private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(): List<ActionableAccount> = withContext(defaultDispatcher) {
        val accounts = accountRepo.getAllAccounts()
        val sims = simRepo.getAll()
        val ussds = accountRepo.getUssdAccounts()

        val result: MutableList<ActionableAccount> = mutableListOf()
        for (account in accounts) {
            if (account.type == USSD_TYPE) {
                val ussdAcct = ussds.find { it.id == account.id }
                val actions = ussdAcct?.let {
                    actionRepository.getActions(ussdAcct.institutionId, ussdAcct.countryAlpha2)
                }
                Timber.e("Loaded ${actions?.size} actions")
                result.add(ActionableAccount(account, sims.find { it.subscriptionId == ussdAcct?.simSubscriptionId }, ussdAcct, null, actions))
            } else {
                val usdcAcct = accountRepo.getUsdcAccount(account.id)
                result.add(ActionableAccount(account, null, null, usdcAcct, null))
            }
        }
        result
    }
}