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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class TransferViewModel(application: Application, repo: DatabaseRepo) : AbstractFormViewModel(application, repo) {

    val amount = MutableLiveData<String?>()
    val contact = MutableLiveData<StaxContact?>()
    val note = MutableLiveData<String?>()
    var request: LiveData<Request> = MutableLiveData()
    var completeAutoFilling: MutableLiveData<AutofillData> = MutableLiveData()

    fun setTransactionType(transaction_type: String) {
        TransactionType.type = transaction_type
    }

    fun setAmount(a: String?) = amount.postValue(a)

    private fun setContact(contactIds: String?) = contactIds?.let {
        viewModelScope.launch {
            val contacts = repo.getContacts(contactIds.split(",").toTypedArray())
            if (contacts.isNotEmpty()) contact.postValue(contacts.first())
        }
    }

    fun autoFill(transactionUUID: String) = viewModelScope.launch(Dispatchers.IO) {
        val transaction = repo.getTransaction(transactionUUID)
        if (transaction != null) {
            val action = repo.getAction(transaction.action_id)

            action?.let {
                val contact = repo.getContactAsync(transaction.counterparty_id)
                autoFill(transaction.amount.toInt().toString(), contact, AutofillData(action.to_institution_id, transaction.channel_id, transaction.accountId, true))
            }
        }
    }

    private fun autoFill(amount: String, contact: StaxContact?, autofillData: AutofillData) {
        setContact(contact)
        setAmount(amount)
        autofillData.institutionId?.let { completeAutoFilling.postValue(autofillData) }
    }

    fun setContact(sc: StaxContact?) = sc?.let {
        contact.postValue(it)
    }

    fun forceUpdateContactUI() = contact.postValue(contact.value)

    fun setRecipient(r: String) {
        if (contact.value != null && contact.value.toString() == r) return
        contact.value = StaxContact(r)
    }

    fun resetRecipient() {
        contact.value = StaxContact()
    }

    fun setRecipientSmartly(contactNum: String?, channel: Channel) {
        contactNum?.let {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val formattedPhone = PhoneHelper.getInternationalNumber(channel.countryAlpha2, it)
                    val sc = repo.getContactByPhone(formattedPhone)
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
        autoFill(r.amount!!, StaxContact(r.requester_number), AutofillData(r.requester_institution_id, -1, -1, r.amount.isNullOrEmpty()))
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

    fun reset() {
        amount.value = null
        contact.value = null
        completeAutoFilling.value = null
    }
}

data class AutofillData(val institutionId: Int?, val channelId: Int, val accountId: Int, val isEditing: Boolean)