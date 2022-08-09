package com.hover.stax.presentation.simcard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.sdk.sims.SimInfo
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.use_case.accounts.GetAccountsUseCase
import com.hover.stax.domain.use_case.sims.GetPresentSimUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SimViewModel(presentSimUseCase: GetPresentSimUseCase,
                   private val getAccountsUseCase: GetAccountsUseCase) : ViewModel() {

	val simSubscriptionIds = MediatorLiveData<IntArray>()
	val telecomAccounts = MediatorLiveData<List<Account>>()
	val presentSims : LiveData<List<SimInfo>> = presentSimUseCase.withLiveData()

	init {
		simSubscriptionIds.addSource(presentSimUseCase.withLiveData(), this::setSubscriptionIds)
		telecomAccounts.addSource(simSubscriptionIds, this::getTelecomAccounts)
	}



	private fun setSubscriptionIds(sims: List<SimInfo>) {
		simSubscriptionIds.postValue(sims.map { it.subscriptionId }.toIntArray())
	}

	private fun getTelecomAccounts(subIds: IntArray){
		viewModelScope.launch(Dispatchers.IO) {
			telecomAccounts.postValue(getAccountsUseCase.telecomAccounts(subIds))
		}
	}

}