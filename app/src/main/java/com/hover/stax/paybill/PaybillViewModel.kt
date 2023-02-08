/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.paybill

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.model.USSDAccount
import com.hover.stax.schedules.ScheduleRepo
import com.hover.stax.transfers.AbstractFormViewModel
import com.hover.stax.utils.AnalyticsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber

class PaybillViewModel(
    application: Application,
    contactRepo: ContactRepo,
    val actionRepo: ActionRepo,
    private val billRepo: PaybillRepo,
    val accountRepo: AccountRepo,
    scheduleRepo: ScheduleRepo
) : AbstractFormViewModel(application, contactRepo, scheduleRepo) {

    val savedPaybills = MutableLiveData<List<Paybill>>()

    val selectedPaybill = MutableLiveData<Paybill?>()

    val businessName = MutableLiveData<String?>()
    val businessNumber = MutableLiveData<String?>()
    val accountNumber = MutableLiveData<String?>()
    val nickname = MutableLiveData<String?>()
    val amount = MutableLiveData<String?>()
    val iconDrawable = MutableLiveData(R.drawable.ic_smile)

    val saveBill = MutableLiveData(false)
    private val saveAmount = MutableLiveData(false)

    fun getSavedPaybills(accountId: Int) = viewModelScope.launch {
        billRepo.getPaybills(accountId).collect { savedPaybills.postValue(it) }
    }

    fun selectPaybill(paybill: Paybill) {
        selectedPaybill.value = paybill

        businessName.value = paybill.businessName
        businessNumber.value = paybill.businessNo
        accountNumber.value = paybill.accountNo
        amount.value = if (paybill.recurringAmount == 0) "" else paybill.recurringAmount.toString()
        saveBill.value = paybill.isSaved
        nickname.value = paybill.name
        iconDrawable.value = paybill.logo
        saveAmount.value = paybill.recurringAmount != 0
    }

    fun selectPaybill(action: HoverAction) {
        deSelectPaybill()
        Timber.e("selecting paybill by action: %s", action.to_institution_name)
        val paybill = Paybill(
            "", action.to_institution_name, Paybill.extractBizNumber(action), null, action.public_id,
            0, getString(R.string.root_url).plus(action.to_institution_logo)
        )
        selectPaybill(paybill)
    }

    private fun deSelectPaybill() {
        iconDrawable.value = R.drawable.ic_edit
        Timber.e("deselecting %s", selectedPaybill.value?.name)
        if (selectedPaybill.value?.name != null) nickname.value = null
        if (selectedPaybill.value?.isSaved == true) saveBill.value = false
        if (selectedPaybill.value?.accountNo != null) accountNumber.value = null
        if (selectedPaybill.value?.recurringAmount != 0) amount.value = null
        saveAmount.value = false
        selectedPaybill.value = null
    }

    fun setBusinessNumber(number: String) {
        deSelectPaybill()
        businessNumber.value = number
        businessName.value = null
    }

    fun setAccountNumber(number: String) {
        accountNumber.value = number
    }

    fun setAmount(value: String) {
        amount.value = value
    }

    fun setSave(shouldSave: Boolean) {
        this.saveBill.value = shouldSave
    }

    fun setSaveAmount(shouldSave: Boolean) {
        this.saveAmount.value = shouldSave
    }

    fun setIconDrawable(drawable: Int) {
        iconDrawable.value = drawable
    }

    fun setNickname(nickname: String) {
        this.nickname.value = nickname
    }

    private fun logPaybill(paybill: Paybill, isSaved: Boolean = true) {
        val data = JSONObject()

        try {
            data.put("name", paybill.name)
            data.put("businessNo", paybill.businessNo)
            data.put("actionId", paybill.actionId)
        } catch (e: Exception) {
            Timber.e(e)
        }

        AnalyticsUtil.logAnalyticsEvent(getString(if (!isSaved) R.string.deleted_paybill else R.string.saved_paybill), data, getApplication())
    }

    fun businessNoError(): String? {
        Timber.e("biz no: %s", businessNumber.value)
        return if (businessNumber.value.isNullOrEmpty())
            getString(R.string.paybill_error_business_number)
        else null
    }

    fun amountError(): String? {
        Timber.e("amount: %s", amount.value)
        return if (!amount.value.isNullOrEmpty() && amount.value!!.matches("[\\d.]+".toRegex()) && !amount.value!!.matches("[0]+".toRegex())) null
        else getString(R.string.amount_fielderror)
    }

    fun accountNoError(): String? {
        Timber.e("acct no: %s", accountNumber.value)
        return if (accountNumber.value.isNullOrEmpty())
            getString(R.string.transfer_error_recipient_account)
        else null
    }

    fun nameError(): String? = if (saveBill.value!! && nickname.value.isNullOrEmpty())
        getString(R.string.bill_name_error)
    else null

    fun deletePaybill(paybill: Paybill) = viewModelScope.launch(Dispatchers.IO) {
        billRepo.delete(paybill)
        logPaybill(paybill, false)
    }

    fun hasEditedSaved(): Boolean {
        val selected = selectedPaybill.value
        Timber.e("is saved? %b", selected?.isSaved)
        return when {
            selected == null || !selected.isSaved -> false
            !saveBill.value!! -> false
            selected.name != nickname.value -> true
            selected.logo != iconDrawable.value -> true
            selected.accountNo != accountNumber.value -> true
            saveAmount.value!! && selected.recurringAmount.toString() != amount.value!! -> true
            else -> false
        }
    }

    fun savePaybill(account: USSDAccount?, action: HoverAction?) {
        viewModelScope.launch(Dispatchers.IO) {
            Timber.e("saving bill")
            val businessNo = businessNumber.value
            val accountNo = accountNumber.value

            if (account != null && action != null) {
                val payBill = Paybill(nickname.value!!, action.to_institution_name, businessNo!!, accountNo, action.public_id, account.id, getIcon(account)).apply {
                    isSaved = true
                    logo = iconDrawable.value ?: 0
                    channelId = account.channelId
                }
                if (saveAmount.value!!) payBill.recurringAmount = amount.value!!.toInt()

                billRepo.save(payBill)
                logPaybill(payBill)
            } else {
                Timber.e("Action or account not set")
            }
        }
        setEditing(false)
    }

    private fun getIcon(account: Account): String {
        return selectedPaybill.value?.logoUrl ?: account.logoUrl
    }

    fun updatePaybill(paybill: Paybill) = viewModelScope.launch(Dispatchers.IO) {
        Timber.e("updating bill")
        with(paybill) {
            name = nickname.value!!
            businessNo = businessNumber.value!!
            accountNo = accountNumber.value!!
            logo = iconDrawable.value ?: 0
            recurringAmount = if (saveAmount.value!!) amount.value!!.toInt() else 0

            billRepo.update(this)
        }
    }

    override fun reset() {
        super.reset()
        Timber.e("resetting")
        viewModelScope.launch {
            selectedPaybill.postValue(null)
            businessName.postValue(null)
            businessNumber.postValue(null)
            accountNumber.postValue(null)
            amount.postValue(null)
            nickname.postValue(null)
            iconDrawable.postValue(0)
            saveBill.postValue(false)
            saveAmount.postValue(false)
        }
    }

    fun wrapExtras(): HashMap<String, String> {
        val extras: HashMap<String, String> = hashMapOf()
        if (amount.value != null) extras[HoverAction.AMOUNT_KEY] = amount.value!!
        if (businessNumber.value != null) extras[BUSINESS_NO] = businessNumber.value!!
        if (accountNumber.value != null) extras[HoverAction.ACCOUNT_KEY] = accountNumber.value!!

        return extras
    }
}