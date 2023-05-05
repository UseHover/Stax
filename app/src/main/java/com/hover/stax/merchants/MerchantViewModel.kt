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
package com.hover.stax.merchants

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.data.contact.ContactRepo
import com.hover.stax.data.merchant.MerchantRepo
import com.hover.stax.database.models.Account
import com.hover.stax.database.models.Merchant
import com.hover.stax.database.models.BUSINESS_NO
import com.hover.stax.data.schedule.ScheduleRepo
import com.hover.stax.transfers.AbstractFormViewModel
import com.hover.stax.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MerchantViewModel(
    application: Application,
    contactRepo: ContactRepo,
    private val merchantRepo: MerchantRepo,
    scheduleRepo: ScheduleRepo
) : AbstractFormViewModel(application, contactRepo, scheduleRepo) {

    val amount = MutableLiveData<String?>()
    val merchant = MutableLiveData<Merchant?>()

    var recentMerchants: LiveData<List<Merchant>> = MutableLiveData()

    init {
        recentMerchants = merchantRepo.all
    }

    fun setAmount(a: String?) = amount.postValue(a)

    fun setMerchant(m: Merchant?) = m?.let { merchant.postValue(it) }

    fun setMerchant(merchantId: Int) = viewModelScope.launch(Dispatchers.IO) {
        merchant.postValue(merchantRepo.get(merchantId))
    }

    fun setMerchant(str: String, account: Account?, action: HoverAction?) {
        if (merchant.value != null && merchant.value.toString() == str) return
        if (account == null || action == null) return
        merchant.value = Merchant(null, str, action.public_id, account.id, account.channelId)
    }

    fun amountErrors(): String? {
        return if (!amount.value.isNullOrEmpty() && amount.value!!.matches("[\\d.]+".toRegex()) && !amount.value!!.matches("[0]+".toRegex())) null
        else getString(R.string.amount_fielderror)
    }

    fun recipientErrors(): String? {
        return when {
            (merchant.value == null || merchant.value?.tillNo == null) -> getString(R.string.paybill_error_business_number)
            else -> null
        }
    }

    fun wrapExtras(): HashMap<String, String> {
        val extras: HashMap<String, String> = hashMapOf()
        if (amount.value != null) extras[HoverAction.AMOUNT_KEY] = amount.value!!
        if (merchant.value != null) {
            extras[BUSINESS_NO] = merchant.value!!.tillNo
        }
        return extras
    }

    fun saveMerchant() {
        merchant.value?.let { merchant ->
            viewModelScope.launch(Dispatchers.IO) {
                merchant.lastUsedTimestamp = DateUtils.now()
                merchantRepo.save(merchant)
            }
        }
    }

    override fun reset() {
        super.reset()
        amount.value = null
        merchant.value = null
    }
}