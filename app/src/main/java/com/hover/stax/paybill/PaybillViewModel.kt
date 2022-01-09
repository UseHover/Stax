package com.hover.stax.paybill

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.R
import com.hover.stax.utils.UIHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PaybillViewModel(val repo: PaybillRepo, val application: Application) : ViewModel() {

    val savedPaybills = MutableLiveData<List<Paybill>>()
    val accountPaybills = MutableLiveData<List<Paybill>>()

    val selectedPaybill = MutableLiveData<Paybill>()
    val businessNumber = MutableLiveData<String>()
    val accountNumber = MutableLiveData<String>()
    val amount = MutableLiveData<String>()
    val iconDrawable = MutableLiveData<Int>()

    fun getSavedPaybills(accountId: Int) = viewModelScope.launch {
        repo.getSavedPaybills(accountId).collect { savedPaybills.postValue(it) }
    }

    fun getPaybills(accountId: Int) = viewModelScope.launch {
        repo.getPaybills(accountId).map { paybills -> paybills.filterNot { it.isSaved } }.collect { accountPaybills.postValue(it) }
    }

    fun selectPaybill(paybill: Paybill){
        selectedPaybill.value = paybill
    }

    fun setBusinessNumber(number: String) {
        businessNumber.value = number
    }

    fun setAccountNumber(number: String) {
        accountNumber.value = number
    }

    fun setAmount(value: String) {
        amount.value = value
    }

    fun setIconDrawable(drawable: Int) {
        iconDrawable.value = drawable
    }

    fun savePaybill(channelId: Int) {

    }

    fun businessNoError(): String? = if(businessNumber.value.isNullOrEmpty())
        application.getString(R.string.paybill_error_business_number)
    else null

    fun amountError(): String? {
        return if (!amount.value.isNullOrEmpty() && amount.value!!.matches("[\\d.]+".toRegex()) && !amount.value!!.matches("[0]+".toRegex())) null
        else application.getString(R.string.amount_fielderror)
    }

    fun accountNoError(): String? = if(accountNumber.value.isNullOrEmpty())
        application.getString(R.string.transfer_error_recipient_account)
    else null

    fun deletePaybill(paybill: Paybill) = viewModelScope.launch(Dispatchers.IO) {
        paybill.isSaved = false
        repo.update(paybill)

        //TODO show this on the main thread
        //UIHelper.flashMessage(application.applicationContext, R.string.paybill_delete_success)
    }
}