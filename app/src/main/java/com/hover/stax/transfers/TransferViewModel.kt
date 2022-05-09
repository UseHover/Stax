package com.hover.stax.transfers

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.contacts.PhoneHelper
import com.hover.stax.contacts.StaxContact
import com.hover.stax.schedules.ScheduleRepo
import com.hover.stax.requests.Request
import com.hover.stax.requests.RequestRepo
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.DateUtils
import com.yariksoffice.lingver.Lingver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class TransferViewModel(application: Application, private val requestRepo: RequestRepo, contactRepo: ContactRepo, scheduleRepo: ScheduleRepo) : AbstractFormViewModel(application, contactRepo, scheduleRepo) {

    val amount = MutableLiveData<String?>()
    val contact = MutableLiveData<StaxContact?>()
    val note = MutableLiveData<String?>()
    var request: MutableLiveData<Request?> = MutableLiveData()

    fun setAmount(a: String?) = amount.postValue(a)

    private fun setContact(contactIds: List<String>?) = contactIds?.let {
        viewModelScope.launch {
            val contacts = contactRepo.getContacts(contactIds.toTypedArray())
            if (contacts.isNotEmpty()) contact.postValue(contacts.first())
        }
    }

    fun setContact(sc: StaxContact?) = sc?.let { contact.postValue(it) }

    fun setContact(contactId: String) = viewModelScope.launch(Dispatchers.IO) {
        contact.postValue(contactRepo.getContact(contactId))
    }

    fun setRecipientNumber(str: String) {
        if (contact.value != null && contact.value.toString() == str) return
        contact.value = if (str.isNullOrEmpty()) StaxContact() else StaxContact(str)
    }

    fun setRecipientSmartly(r: Request?, countryAlpha2: String?) {
        r?.let {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val formattedPhone = PhoneHelper.getNationalSignificantNumber(r.requester_number, countryAlpha2 ?: Lingver.getInstance().getLocale().country)
                    val sc = contactRepo.getContactByPhone(formattedPhone)
                    sc?.let { contact.postValue(it) }
                } catch (e: NumberFormatException) {
                    AnalyticsUtil.logErrorAndReportToFirebase(TransferViewModel::class.java.simpleName, e.message!!, e)
                }
            }
        }
    }

    private fun setNote(n: String?) = note.postValue(n)

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

    fun decrypt(encryptedString: String): LiveData<Request?> {
        viewModelScope.launch {
            request = requestRepo.decrypt(encryptedString, getApplication())
        }
        return request
    }

    fun load(r: Request) {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.loaded_request_link), getApplication())
        setRecipientSmartly(r, r.requester_country_alpha2)
        setAmount(r.amount)
        setContact(r.requestee_ids.split(","))
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

    override fun reset() {
        super.reset()
        amount.value = null
        contact.value = null
        note.value = null
        request.value = null
    }
}