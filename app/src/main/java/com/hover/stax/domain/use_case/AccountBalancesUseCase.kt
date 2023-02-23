package com.hover.stax.domain.use_case

import com.hover.sdk.actions.HoverAction
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.model.USDCAccount
import com.hover.stax.domain.model.USSDAccount
import com.hover.stax.domain.model.USSD_TYPE
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber

data class AccountWithBalance(
	val account: Account,
	val ussdAccount: USSDAccount?,
	val usdcAccount: USDCAccount?,
	val balanceAction: HoverAction?
)

class AccountBalancesUseCase(
	val accountRepo: AccountRepo,
	private val actionRepository: ActionRepo,
	private val defaultDispatcher: CoroutineDispatcher
) {

	suspend operator fun invoke(): List<AccountWithBalance> = withContext(defaultDispatcher) {
		Timber.e("loading accounts in use case")
		val accounts = accountRepo.getAllAccounts()
		val ussds = accountRepo.getUssdAccounts()

		val result: MutableList<AccountWithBalance> = mutableListOf()
		for (account in accounts) {
			if (account.type == USSD_TYPE) {
				val ussdAcct = ussds.find { it.id == account.id }
				if (ussdAcct?.institutionType == null || ussdAcct.institutionType == "telecom") { Timber.e("Found telco, breaking"); break }
				val balanceAct = ussdAcct.let {
					actionRepository.getFirstAction(ussdAcct.institutionId, ussdAcct.countryAlpha2, HoverAction.BALANCE)
				}
				result.add(AccountWithBalance(account, ussdAcct, null, balanceAct))
			} else {
				val usdcAcct = accountRepo.getUsdcAccount(account.id)
				result.add(AccountWithBalance(account, null, usdcAcct, null))
			}
		}
		result
	}
}