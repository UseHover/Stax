package com.hover.stax.requests

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.channels.Channel
import com.hover.stax.contacts.StaxContact
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.schedules.Schedule
import com.hover.stax.transfers.AbstractFormViewModel
import com.hover.stax.transfers.AutoFillTransferInfo
import com.hover.stax.utils.Constants
import com.hover.stax.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class NewRequestViewModel(application: Application, databaseRepo: DatabaseRepo) : AbstractFormViewModel(application, databaseRepo) {

    val activeAccount = MutableLiveData<Account?>()
    val activeChannel = MutableLiveData<Channel>()
    val amount = MutableLiveData<String?>()
    val requestees = MutableLiveData<List<StaxContact>>(Collections.singletonList(StaxContact("")))
    val requestee = MutableLiveData<StaxContact?>()
    val requesterNumber = MediatorLiveData<String>()
    val note = MutableLiveData<String?>()

    val formulatedRequest = MutableLiveData<Request>()
    private val finalRequests = MutableLiveData<List<Request>>()

    init {
        requesterNumber.addSource(activeChannel, this::setRequesterNumber)
    }

    fun setAmount(a: String?) = amount.postValue(a)

    fun setActiveChannel(c: Channel) {
        activeChannel.postValue(c)

        viewModelScope.launch(Dispatchers.IO) {
            val account = repo.getAccounts(c.id).firstOrNull()
            setActiveAccount(account)
        }
    }

    private fun setActiveAccount(account: Account?) = activeAccount.postValue(account)

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

    fun setNote(n: String?) = note.postValue(n)

    fun validAmount(): Boolean = (!amount.value.isNullOrEmpty() && amount.value!!.matches("\\d+".toRegex()) && !amount.value!!.matches("[0]+".toRegex()))

    fun requesteeErrors(): String? {
        return if (!requestee.value?.accountNumber.isNullOrEmpty())
            null
        else
            application.getString(R.string.request_error_recipient)
    }

    fun accountError(): String? = if (activeAccount.value != null) null else application.getString(R.string.accounts_error_noselect)

    fun isValidAccount(): Boolean = activeAccount.value!!.name != Constants.PLACEHOLDER

    fun requesterAcctNoError(): String? = if (!requesterNumber.value.isNullOrEmpty()) null else application.getString(R.string.requester_number_fielderror)

    fun validNote(): Boolean = !note.value.isNullOrEmpty()

    //TODO validate that this works from schedule
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

        val transferAutoFill = AutoFillTransferInfo()
        transferAutoFill.amount = amount.value
        transferAutoFill.note = note.value
        transferAutoFill.toInstitutionId = activeChannel.value!!.institutionId
        transferAutoFill.contactNumber = requesterNumber.value



        val request = Request(transferAutoFill)
        formulatedRequest.value = request
    }

    fun saveRequest() {
        if (formulatedRequest.value != null) {
            val request = Request(formulatedRequest.value!!, requestee.value, application)
            repo.insert(request)

            finalRequests.value = listOf(request)
        }
    }

    private fun saveContacts() {
        requestee.value?.let { contact ->
            viewModelScope.launch {
                if (!contact.accountNumber.isNullOrEmpty()) {
                    contact.lastUsedTimestamp = DateUtils.now()
                    repo.save(contact)
                }
            }
        }
    }

    fun reset() {
        setAmount(null)
        setNote(null)
        requestee.value = null
    }
}