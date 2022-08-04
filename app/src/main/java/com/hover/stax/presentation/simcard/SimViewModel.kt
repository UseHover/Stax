package com.hover.stax.presentation.simcard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.sdk.sims.SimInfo
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.use_case.sims.GetLivePresentSimUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SimViewModel(getLivePresentSimUseCase: GetLivePresentSimUseCase,
                   val repo: AccountRepo) : ViewModel() {

	val simSubscriptionIds = MediatorLiveData<IntArray>()
	val telecomAccounts = MediatorLiveData<List<Account>>()
	val presentSims : LiveData<List<SimInfo>> = getLivePresentSimUseCase()

	init {
		simSubscriptionIds.addSource(getLivePresentSimUseCase(), this::setSubscriptionIds)
		telecomAccounts.addSource(simSubscriptionIds, this::getTelecomAccounts)
	}

	private fun setSubscriptionIds(sims: List<SimInfo>) {
		simSubscriptionIds.postValue(sims.map { it.subscriptionId }.toIntArray())
	}

	private fun getTelecomAccounts(subIds: IntArray){
		viewModelScope.launch(Dispatchers.IO) {
			telecomAccounts.postValue(repo.getTelecomAccounts(subIds))
		}
	}

}