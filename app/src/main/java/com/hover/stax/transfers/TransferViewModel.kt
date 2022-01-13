package com.hover.stax.transfers

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.contacts.PhoneHelper
import com.hover.stax.contacts.StaxContact
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.requests.Request
import com.hover.stax.schedules.Schedule
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.DateUtils
import com.hover.stax.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class TransferViewModel(application: Application, repo: DatabaseRepo) : AbstractFormViewModel(application, repo) {

    val amount = MutableLiveData<String>()
    val contact = MutableLiveData<StaxContact>()
    val note = MutableLiveData<String>()
    var request: LiveData<Request> = MutableLiveData()
    private var nonTemplateVariables   =  MutableLiveData<HashMap<String, String>>()

    fun setTransactionType(transaction_type: String) {
        TransactionType.type = transaction_type
    }

    fun setAmount(a: String) = amount.postValue(a)

    private fun setContact(contactIds: String?) = contactIds?.let {
        viewModelScope.launch {
            val contacts = repo.getContacts(contactIds.split(",").toTypedArray())
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
                    val sc = repo.getContactByPhone(formattedPhone)
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
        else application.getString(R.string.amount_fielderror)
    }

    fun recipientErrors(a: HoverAction?): String? {
        return when {
            (a != null && a.requiresRecipient() && (contact.value == null || contact.value?.accountNumber == null)) -> application.getString(if (a.isPhoneBased) R.string.transfer_error_recipient_phone else R.string.transfer_error_recipient_account)
            else -> null
        }
    }

    fun decrypt(encryptedString: String): LiveData<Request> {
        request = repo.decrypt(encryptedString, application)
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
                repo.update(it)
            }
        }
    }

    fun saveContact() {
        contact.value?.let { sc ->
            viewModelScope.launch {
                sc.lastUsedTimestamp = DateUtils.now()
                repo.save(sc)
            }
        }
    }

    fun updateNonTemplateVariables(key: String, value: String) {
        var currentMap = nonTemplateVariables.value
        if(currentMap == null) currentMap = HashMap()

        currentMap.put(key, value)
        nonTemplateVariables.postValue(currentMap);

    }

    fun reset() {
        amount.value = null
        contact.value = null
    }
}