package com.hover.stax.transfers

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.contacts.PhoneHelper
import com.hover.stax.contacts.StaxContact
import com.hover.stax.schedules.ScheduleRepo
import com.hover.stax.requests.Request
import com.hover.stax.requests.RequestRepo
import com.hover.stax.schedules.Schedule
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants
import com.hover.stax.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransferViewModel(application: Application, val requestRepo: RequestRepo, contactRepo: ContactRepo, scheduleRepo: ScheduleRepo) : AbstractFormViewModel(application, contactRepo, scheduleRepo) {

    val amount = MutableLiveData<String?>()
    val contact = MutableLiveData<StaxContact?>()
    val note = MutableLiveData<String>()
    var request: LiveData<Request> = MutableLiveData()

    fun setTransactionType(transaction_type: String) {
        TransactionType.type = transaction_type
    }

    fun setAmount(a: String) = amount.postValue(a)

    private fun setContact(contactIds: String?) = contactIds?.let {
        viewModelScope.launch {
            val contacts = contactRepo.getContacts(contactIds.split(",").toTypedArray())
            if (contacts.isNotEmpty()) contact.postValue(contacts.first())
        }
    }

    fun setContact(sc: StaxContact?) = sc?.let { contact.postValue(it) }

    fun forceUpdateContactUI() = contact.postValue(contact.value)

    fun setRecipient(r: String) {
        if (contact.value != null && contact.value.toString() == r) return
        contact.value = StaxContact(r)
    }

    fun resetRecipient() {
        contact.value = StaxContact()
    }

    fun setRecipientSmartly(r: Request?, channel: Channel) {
        r?.let {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val formattedPhone = PhoneHelper.getInternationalNumber(channel.countryAlpha2, r.requester_number)
                    val sc = contactRepo.getContactByPhone(formattedPhone)
                    sc?.let { contact.postValue(it) }
                } catch (e: NumberFormatException) {
                    AnalyticsUtil.logErrorAndReportToFirebase(TransferViewModel::class.java.simpleName, e.message!!, e)
                }
            }
        }
    }

    private fun setNote(n: String) = note.postValue(n)

    fun amountErrors(): String? {
        return if (!amount.value.isNullOrEmpty() && amount.value!!.matches("[\\d.]+".toRegex()) && !amount.value!!.matches("[0]+".toRegex())) null
        else getString(R.string.amount_fielderror)
    }

    fun recipientErrors(a: HoverAction?): String? {
        return when {
            (a != null && a.requiresRecipient() && (contact.value == null || contact.value?.accountNumber == null)) -> getString(if (a.isPhoneBased) R.string.transfer_error_recipient_phone else R.string.transfer_error_recipient_account)
            else -> null
        }
    }

    fun wrapExtras(): HashMap<String, String> {
        val extras: HashMap<String, String> = hashMapOf()
        if (amount.value != null) extras[HoverAction.AMOUNT_KEY] = amount.value!!
        if (contact.value != null) {
            extras[StaxContact.ID_KEY] = contact.value!!.id
            extras[HoverAction.PHONE_KEY] = contact.value!!.accountNumber
            extras[HoverAction.ACCOUNT_KEY] = contact.value!!.accountNumber
        }
        if (note.value != null) extras[HoverAction.NOTE_KEY] = note.value!!
        return extras
    }

    fun decrypt(encryptedString: String): LiveData<Request> {
        request = requestRepo.decrypt(encryptedString, getApplication())
        return request
    }

    fun view(s: Schedule) {
        schedule.postValue(s)
        setTransactionType(s.type)
        setAmount(s.amount)
        setContact(s.recipient_ids)
        setNote(s.note)
    }

    fun view(r: Request) {
        setAmount(r.amount)
        setContact(r.requestee_ids)
        setNote(r.note)
    }

    fun checkSchedule() {
        schedule.value?.let {
            if (it.end_date <= DateUtils.today()) {
                it.complete = true
                scheduleRepo.update(it)
            }
        }
    }

    fun saveContact() {
        contact.value?.let { sc ->
            viewModelScope.launch {
                sc.lastUsedTimestamp = DateUtils.now()
                contactRepo.save(sc)
            }
        }
    }

    fun reset() {
        amount.value = null
        contact.value = null
    }
}