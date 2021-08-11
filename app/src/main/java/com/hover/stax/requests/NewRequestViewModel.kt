package com.hover.stax.requests

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.contacts.StaxContact
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.schedules.Schedule
import com.hover.stax.transfers.AbstractFormViewModel
import com.hover.stax.utils.DateUtils
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class NewRequestViewModel(application: Application, databaseRepo: DatabaseRepo) : AbstractFormViewModel(application, databaseRepo) {

    val activeChannel = MediatorLiveData<Channel>()
    val amount = MutableLiveData<String>()
    val requestees = MutableLiveData<List<StaxContact>>(Collections.singletonList(StaxContact("")))
    val requestee = MutableLiveData<StaxContact>()
    val requesterNumber = MediatorLiveData<String>()
    val note = MutableLiveData<String>()

    val formulatedRequest = MutableLiveData<Request>()
    private val finalRequests = MutableLiveData<List<Request>>()

    init {
        requesterNumber.addSource(activeChannel, this::setRequesterNumber)
    }

    fun setAmount(a: String) = amount.postValue(a)

    fun setActiveChannel(c: Channel) = activeChannel.postValue(c)

    private fun setRequesterNumber(c: Channel) = requesterNumber.postValue(c.accountNo)

    fun setRequesterNumber(number: String) {
        requesterNumber.postValue(number)
        activeChannel.value?.let { it.accountNo = number }
    }

    fun setRecipient(recipient: String) {
        if (requestee.value != null && requestee.value.toString() == recipient) return
        requestee.value = StaxContact(recipient)
    }

    fun addRecipient(contact: StaxContact) {
        requestee.value = contact
    }

    fun setNote(n: String) = note.postValue(n)

    fun validAmount(): Boolean = (!amount.value.isNullOrEmpty() && amount.value!!.matches("\\d+".toRegex()) && !amount.value!!.matches("[0]+".toRegex()))

    fun requesteeErrors(): String? {
        return if (!requestee.value?.accountNumber.isNullOrEmpty())
            null
        else
            application.getString(R.string.request_error_recipient)
    }

    fun requesterAcctNoError(): String? = if (!requesterNumber.value.isNullOrEmpty()) null else application.getString(R.string.requester_number_fielderror)

    fun validNote(): Boolean = !note.value.isNullOrEmpty()

    fun setSchedule(s: Schedule) {
        schedule.postValue(s)
        setAmount(s.amount)

        viewModelScope.launch {
            val contacts = repo.getContacts(s.recipient_ids.split(",").toTypedArray())
            requestees.postValue(contacts)
        }
    }

    fun createRequest() {
        repo.update(activeChannel.value)
        saveContacts()

        val request = Request(amount.value, note.value, requesterNumber.value, activeChannel.value!!.institutionId)
        formulatedRequest.value = request
    }

    fun saveRequest() {
        if (!finalRequests.value.isNullOrEmpty() && requestees.value != null && finalRequests.value!!.size == requestees.value!!.size)
            return

        val requests = ArrayList<Request>()
        requestees.value!!.forEach { recipient ->
            val request = Request(formulatedRequest.value!!, recipient, application)
            requests.add(request)
            repo.insert(request)
        }

        finalRequests.value = if (!requests.isNullOrEmpty()) requests else null
    }

    fun removeInvalidRequestees() {
        if (!requestees.value.isNullOrEmpty()) {
            val contacts = ArrayList<StaxContact>()

            requestees.value?.forEach { contact ->
                if (!contact.accountNumber.isNullOrEmpty()) contacts.add(contact)
            }

            requestees.postValue(contacts)
        }
    }

    private fun saveContacts() {
        requestees.value?.let { contacts ->
            viewModelScope.launch {
                contacts.filter { contact -> !contact.accountNumber.isNullOrEmpty() }
                    .forEach { contact ->
                        contact.lastUsedTimestamp = DateUtils.now()
                        repo.save(contact)
                    }
            }
        }
    }
}