package com.hover.stax.presentation.simcard

import androidx.lifecycle.*
import com.hover.sdk.sims.SimInfo
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.use_case.accounts.CreateAccountsUseCase
import com.hover.stax.domain.use_case.accounts.GetAccountsUseCase
import com.hover.stax.domain.use_case.sims.GetPresentSimUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class SimViewModel(presentSimUseCase: GetPresentSimUseCase,
                   private val getAccountsUseCase: GetAccountsUseCase,
                   private val createAccountsUseCase: CreateAccountsUseCase) : ViewModel() {

	val simSubscriptionIds = MediatorLiveData<IntArray>()
	val telecomAccounts = MediatorLiveData<List<Account>>()
	val presentSims : LiveData<List<SimInfo>> = presentSimUseCase.withLiveData()

	init {
		simSubscriptionIds.addSource(presentSims, this::setSubscriptionIds)
		telecomAccounts.addSource(simSubscriptionIds, this::setTelecomAccounts)
	}

	private fun setSubscriptionIds(sims: List<SimInfo>) {
		simSubscriptionIds.postValue(sims.map { it.subscriptionId }.toIntArray())
	}

	private fun setTelecomAccounts(subIds: IntArray){
		viewModelScope.launch(Dispatchers.IO) {
			val accounts = getAccountsUseCase.telecomAccounts(subIds)
			telecomAccounts.postValue(accounts)
			createAccountForSimsIfRequired(accounts, presentSims.value!!)
		}
	}

	private suspend fun createAccountForSimsIfRequired(telecomAccounts: List<Account>, presentSims: List<SimInfo>) {
		getSimsHavingNoTelecomAccount(telecomAccounts, presentSims).also {
			createAccountsUseCase.createTelecomAccounts(it)
			Timber.i("total unlinked sim is: ${it.size}")
		}
	}

	private fun getSimsHavingNoTelecomAccount(accounts: List<Account>, sims: List<SimInfo>) : List<SimInfo> {
		return sims.filter {  accounts.find { account-> account.subscriptionId == it.subscriptionId } == null  }
	}

}