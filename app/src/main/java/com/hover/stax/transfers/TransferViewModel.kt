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
package com.hover.stax.transfers

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.data.contact.ContactRepo
import com.hover.stax.database.models.PhoneHelper
import com.hover.stax.database.models.StaxContact
import com.hover.stax.database.models.Request
import com.hover.stax.data.requests.RequestRepo
import com.hover.stax.data.schedule.ScheduleRepo
import com.hover.stax.core.AnalyticsUtil
import com.hover.stax.core.DateUtils
import com.yariksoffice.lingver.Lingver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class TransferViewModel(
    application: Application,
    private val requestRepo: RequestRepo,
    contactRepo: ContactRepo,
    scheduleRepo: ScheduleRepo
) : AbstractFormViewModel(application, contactRepo, scheduleRepo) {

    val amount = MutableLiveData<String?>()
    val contact = MutableLiveData<StaxContact?>()
    val note = MutableLiveData<String?>()

    val isLoading = MutableLiveData(false)

    fun setAmount(a: String?) = amount.postValue(a)

    fun setContact(contactId: String) = viewModelScope.launch(Dispatchers.IO) {
        contact.postValue(contactRepo.getContact(contactId))
    }

    fun setContact(sc: StaxContact?) = sc?.let {
        contact.postValue(it)
    }

    fun setRecipientNumber(str: String) {
        if (contact.value != null && contact.value.toString() == str) return
        if (str.isEmpty()) {
            contact.value =
                StaxContact()
        } else {
            contact.value = StaxContact(str)
        }
    }

    private fun setRecipientSmartly(r: Request?, countryAlpha2: String?) =
        viewModelScope.launch(Dispatchers.IO) {
            r?.let {
                try {
                    val formattedPhone = PhoneHelper.getNationalSignificantNumber(
                        it.requester_number!!,
                        countryAlpha2 ?: Lingver.getInstance().getLocale().country
                    )
                    val sc = contactRepo.getContactByPhone(formattedPhone)
                    contact.postValue(
                        sc ?: StaxContact(
                            r.requester_number
                        )
                    )
                    isLoading.postValue(false)
                } catch (e: NumberFormatException) {
                    com.hover.stax.core.AnalyticsUtil.logErrorAndReportToFirebase(
                        TransferViewModel::class.java.simpleName, e.message!!, e
                    )
                }
            }
        }

    private fun setNote(n: String?) = note.postValue(n)

    fun amountErrors(a: HoverAction?): String? {
        val regex = a?.getStepByVar(HoverAction.AMOUNT_KEY)?.optString("valid_response_regex")
        return if ((amount.value.isNullOrEmpty() || !amount.value!!.matches("[\\d.]+".toRegex()) || amount.value!!.matches("[0]+".toRegex())) ||
            (!regex.isNullOrEmpty() && !amount.value!!.matches(regex.toRegex()))
        ) getString(R.string.amount_fielderror)
        else null
    }

    fun recipientErrors(a: HoverAction?): String? {
        return when {
            (a != null && a.requiresRecipient() && (contact.value == null || contact.value?.accountNumber.isNullOrEmpty())) -> getString(if (a.isPhoneBased) R.string.transfer_error_recipient_phone else R.string.transfer_error_recipient_account)
            else -> null
        }
    }

    fun wrapExtras(action: HoverAction): HashMap<String, String> {
        val extras: HashMap<String, String> = hashMapOf()
        if (amount.value != null) extras[HoverAction.AMOUNT_KEY] = amount.value!!
        if (contact.value != null && contact.value?.accountNumber != null) {
            extras[StaxContact.ID_KEY] = contact.value!!.id
            extras[HoverAction.PHONE_KEY] = contact.value!!.accountNumber
            extras[HoverAction.ACCOUNT_KEY] = generateRecipientAccount(action)
        }
        if (note.value != null) extras[HoverAction.NOTE_KEY] = note.value!!
        return extras
    }

    private fun generateRecipientAccount(action: HoverAction): String {
        return if (action.bonus_percent > 0 && action.getStepByVar("account").has("prefix")) {
            val prefix = action.getStepByVar("account").optString("prefix")
            if (action.isPhoneBased)
                prefix + PhoneHelper.normalizeNumberByCountry(
                    contact.value!!.accountNumber, action.country_alpha2, action.to_country_alpha2
                )
            else prefix + contact.value!!.accountNumber
        } else contact.value!!.accountNumber
    }

    fun load(encryptedString: String) = viewModelScope.launch {
        isLoading.postValue(true)
        val r: Request? = requestRepo.decrypt(encryptedString, getApplication())
        Timber.v("Loaded request %s", r)
        r?.let {
            setRecipientSmartly(r, r.requester_country_alpha2)
            setAmount(r.amount)
            setNote(r.note)
            com.hover.stax.core.AnalyticsUtil.logAnalyticsEvent(getString(R.string.loaded_request_link), getApplication())
        }
    }

    fun saveContact() {
        viewModelScope.launch(Dispatchers.IO) {
            contact.value?.let { sc ->
                val lookup = StaxContact.getContactByPhoneValue(sc.accountNumber, "", contactRepo)
                val c = if (!sc.hasName() && lookup != null) {
                    lookup
                } else {
                    sc
                }
                c.lastUsedTimestamp = com.hover.stax.core.DateUtils.now()
                contactRepo.save(c)
                contact.postValue(c)
            }
        }
    }

    override fun reset() {
        super.reset()
        amount.value = null
        contact.value = null
        note.value = null
    }
}