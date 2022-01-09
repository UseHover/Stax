package com.hover.stax.paybill

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.utils.UIHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class PaybillViewModel(val repo: PaybillRepo, val application: Application) : ViewModel() {

    val savedPaybills = MutableLiveData<List<Paybill>>()
    val accountPaybills = MutableLiveData<List<Paybill>>()

    val selectedPaybill = MutableLiveData<Paybill>()
    val businessNumber = MutableLiveData<String>()
    val accountNumber = MutableLiveData<String>()
    val name = MutableLiveData<String>()
    val amount = MutableLiveData<String>()
    val iconDrawable = MutableLiveData<Int>()

    fun getSavedPaybills(accountId: Int) = viewModelScope.launch {
        repo.getSavedPaybills(accountId).collect { savedPaybills.postValue(it) }
    }

    fun getPaybills(accountId: Int) = viewModelScope.launch {
        repo.getPaybills(accountId).map { paybills -> paybills.filterNot { it.isSaved } }.collect { accountPaybills.postValue(it) }
    }

    fun selectPaybill(paybill: Paybill) {
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

    fun setNickname(nickname: String) {
        name.value = nickname
    }

    fun savePaybill(account: Account?, recurringAmount: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        val businessNo = businessNumber.value
        val accountNo = accountNumber.value

        if (account != null) {
            val payBill = Paybill(name.value!!, businessNo!!, accountNo, account.channelId, account.id, account.logoUrl)
            if (recurringAmount) payBill.recurringAmount = amount.value!!.toInt()
            payBill.isSaved = true

            repo.save(payBill)

            launch(Dispatchers.Main) {
                UIHelper.flashMessage(application.applicationContext, R.string.paybill_save_success) //TODO add to other language strings
            }
        } else {
            Timber.e("Active account not set")
        }
    }

    fun businessNoError(): String? = if (businessNumber.value.isNullOrEmpty())
        application.getString(R.string.paybill_error_business_number)
    else null

    fun amountError(): String? {
        return if (!amount.value.isNullOrEmpty() && amount.value!!.matches("[\\d.]+".toRegex()) && !amount.value!!.matches("[0]+".toRegex())) null
        else application.getString(R.string.amount_fielderror)
    }

    fun accountNoError(): String? = if (accountNumber.value.isNullOrEmpty())
        application.getString(R.string.transfer_error_recipient_account)
    else null

    fun nameError(): String? = if (name.value.isNullOrEmpty())
        application.getString(R.string.bill_name_error)
    else null

    fun deletePaybill(paybill: Paybill) = viewModelScope.launch(Dispatchers.IO) {
        paybill.isSaved = false
        repo.update(paybill)

        launch(Dispatchers.Main) {
            UIHelper.flashMessage(application.applicationContext, R.string.paybill_delete_success)
        }
    }
}