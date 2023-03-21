package com.hover.stax.domain.use_case

import com.hover.sdk.actions.HoverAction
import com.hover.stax.channels.Channel
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.data.local.channels.ChannelRepo
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.model.USDCAccount
import com.hover.stax.domain.model.USSDAccount
import com.hover.stax.domain.model.USSD_TYPE
import com.hover.stax.transactions.TransactionHistoryItem
import com.hover.stax.transactions.TransactionRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

data class AccountDetail(
	val account: Account,
	val ussdAccount: USSDAccount?,
	val usdcAccount: USDCAccount?,
	val channel: Channel?,
	val transactions: List<TransactionHistoryItem>?,
	val balanceAction: HoverAction?

)

class GetAccountDetailsUseCase(
	val accountRepo: AccountRepo,
	private val channelRepo: ChannelRepo,
	private val transactionRepo: TransactionRepo,
	private val actionRepository: ActionRepo,
	private val defaultDispatcher: CoroutineDispatcher
) {

	suspend operator fun invoke(id: Int, type: String): AccountDetail? = withContext(defaultDispatcher) {
		val account = accountRepo.getAccount(id, type)

		var result: AccountDetail? = null
		if (account?.type == USSD_TYPE) {
			val ussdAcct = accountRepo.getUssdAccount(id)
			var channel: Channel? = null
			var transactions: List<TransactionHistoryItem>? = null
			var action: HoverAction? = null
			ussdAcct?.let {
				channel = channelRepo.getChannel(ussdAcct.channelId)
				transactions = getTransactionsAsHistory(ussdAcct)
				action = actionRepository.getFirstAction(it.institutionId, it.countryAlpha2, HoverAction.BALANCE)
			}
			result = AccountDetail(account, ussdAcct, null, channel, transactions, action)
		} else if (account != null) {
			val usdcAcct = accountRepo.getUsdcAccount(id)
			result = AccountDetail(account, null, usdcAcct, null, null, null)
		}
		result
	}

	private fun getTransactionsAsHistory(ussdAccount: USSDAccount): List<TransactionHistoryItem> {
		val transactions = transactionRepo.getAccountTransactions(ussdAccount)
		val historyList: MutableList<TransactionHistoryItem> = mutableListOf()
		transactions?.asSequence()?.map {
			val action = actionRepository.getAction(it.action_id)
			var institutionName = ""
			action?.let {
				institutionName = action.from_institution_name
			}
			historyList.add(TransactionHistoryItem(it, action, institutionName))
		}
		return historyList
	}
}