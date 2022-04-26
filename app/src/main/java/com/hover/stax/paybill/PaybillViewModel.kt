package com.hover.stax.paybill

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.AccountRepo
import com.hover.stax.actions.ActionRepo
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.paybill.BUSINESS_NO
import com.hover.stax.schedules.ScheduleRepo
import com.hover.stax.transfers.AbstractFormViewModel
import com.hover.stax.utils.AnalyticsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber

class PaybillViewModel(application: Application, contactRepo: ContactRepo, val actionRepo: ActionRepo, val billRepo: PaybillRepo, val accountRepo: AccountRepo, scheduleRepo: ScheduleRepo) : AbstractFormViewModel(application, contactRepo, scheduleRepo) {

    val savedPaybills = MutableLiveData<List<Paybill>>()
    val popularPaybills = MutableLiveData<List<HoverAction>>()

    val selectedPaybill = MutableLiveData<Paybill?>()
    val businessNumber = MutableLiveData<String?>()
    val accountNumber = MutableLiveData<String?>()
    val nickname = MutableLiveData<String?>()
    val amount = MutableLiveData<String?>()
    val iconDrawable = MutableLiveData(0)

    val selectedAction = MutableLiveData<HoverAction>()

    fun getSavedPaybills(accountId: Int) = viewModelScope.launch {
        billRepo.getSavedPaybills(accountId).collect { savedPaybills.postValue(it) }
    }

    fun getPopularPaybills(accountId: Int) = viewModelScope.launch(Dispatchers.IO) {
        accountRepo.getAccount(accountId)?.let {
            val actions = actionRepo.getActions(it.channelId, HoverAction.C2B).filterNot { action -> action.to_institution_id == 0 }
            popularPaybills.postValue(actions)
        }
    }

    fun selectPaybill(paybill: Paybill) {
        selectedPaybill.value = paybill
    }

    fun selectAction(action: HoverAction) {
        selectedAction.value = action

        val paybill = Paybill(
                action.to_institution_name, action.to_institution_id.toString(), null, action.channel_id,
                0, getString(R.string.root_url).plus(action.to_institution_logo)
        )
        selectPaybill(paybill)
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
        this.nickname.value = nickname
    }

    fun savePaybill(account: Account?, recurringAmount: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        val businessNo = businessNumber.value
        val accountNo = accountNumber.value

        if (account != null) {
            val payBill = Paybill(nickname.value!!, businessNo!!, accountNo, account.channelId, account.id, selectedPaybill.value?.logoUrl
                    ?: account.logoUrl).apply {
                isSaved = true
                logo = iconDrawable.value ?: 0
            }
            if (recurringAmount) payBill.recurringAmount = amount.value!!.toInt()

            billRepo.save(payBill)

            logPaybill(payBill)
        } else {
            Timber.e("Active account not set")
        }
    }

    private fun logPaybill(paybill: Paybill, isSaved: Boolean = true) {
        val data = JSONObject()

        try {
            data.put("name", paybill.name)
            data.put("businessNo", paybill.businessNo)
            data.put("channelId", paybill.channelId)
        } catch (e: Exception) {
            Timber.e(e)
        }

        AnalyticsUtil.logAnalyticsEvent(getString(if (!isSaved) R.string.deleted_paybill else R.string.saved_paybill), data, getApplication())
    }

    fun businessNoError(): String? = if (businessNumber.value.isNullOrEmpty())
        getString(R.string.paybill_error_business_number)
    else null

    fun amountError(): String? {
        return if (!amount.value.isNullOrEmpty() && amount.value!!.matches("[\\d.]+".toRegex()) && !amount.value!!.matches("[0]+".toRegex())) null
        else getString(R.string.amount_fielderror)
    }

    fun accountNoError(): String? = if (accountNumber.value.isNullOrEmpty())
        getString(R.string.transfer_error_recipient_account)
    else null

    fun nameError(): String? = if (nickname.value.isNullOrEmpty())
        getString(R.string.bill_name_error)
    else null

    fun deletePaybill(paybill: Paybill) = viewModelScope.launch(Dispatchers.IO) {
        billRepo.delete(paybill)
        logPaybill(paybill, false)
    }

    fun updatePaybill(paybill: Paybill, setRecurringAmount: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        with(paybill) {
            apply {
                name = nickname.value!!
                businessNo = businessNumber.value!!
                accountNo = accountNumber.value!!
                logo = iconDrawable.value ?: 0
                recurringAmount = if (setRecurringAmount) amount.value!!.toInt() else 0
            }

            billRepo.update(this)
        }
    }

    fun reset() = viewModelScope.launch {
        selectedPaybill.postValue(null)
        businessNumber.postValue(null)
        accountNumber.postValue(null)
        amount.postValue(null)
        nickname.postValue(null)
        iconDrawable.postValue(0)
    }

    fun wrapExtras(): HashMap<String, String> {
        val extras: HashMap<String, String> = hashMapOf()
        if (amount.value != null) extras[HoverAction.AMOUNT_KEY] = amount.value!!
        if (businessNumber.value != null) extras[BUSINESS_NO] = businessNumber.value!!
        if (accountNumber.value != null) extras[HoverAction.ACCOUNT_KEY] = accountNumber.value!!
        return extras
    }
}