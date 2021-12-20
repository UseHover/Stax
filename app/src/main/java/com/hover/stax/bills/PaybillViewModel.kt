package com.hover.stax.bills

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PaybillViewModel(val repo: PaybillRepo) : ViewModel() {

    val savedPaybills = MutableLiveData<List<Paybill>>()
    val allPaybills = MutableLiveData<List<Paybill>>()

    fun getPaybills(accountId: Int) = viewModelScope.launch {
        repo.getPaybills(accountId).collect { paybills ->
            savedPaybills.postValue(paybills.filter { it.isSaved })
            allPaybills.postValue(paybills.filterNot { it.isSaved })
        }
    }

    fun getPaybills() = viewModelScope.launch {
        repo.allBills.collect { allPaybills.postValue(it) }
    }
}