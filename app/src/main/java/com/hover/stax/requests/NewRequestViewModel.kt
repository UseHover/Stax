package com.hover.stax.requests

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.AccountRepo
import com.hover.stax.accounts.PLACEHOLDER
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.contacts.StaxContact
import com.hover.stax.schedules.ScheduleRepo
import com.hover.stax.schedules.Schedule
import com.hover.stax.transfers.AbstractFormViewModel
import com.hover.stax.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class NewRequestViewModel(application: Application, val repo: RequestRepo, val accountRepo: AccountRepo, contactRepo: ContactRepo, scheduleRepo: ScheduleRepo) : AbstractFormViewModel(application, contactRepo, scheduleRepo) {

    val activeAccount = MutableLiveData<Account?>()
    val amount = MutableLiveData<String?>()
    private val requestees = MutableLiveData<List<StaxContact>>(Collections.singletonList(StaxContact("")))
    val requestee = MutableLiveData<StaxContact>()
    var requesterNumber = MediatorLiveData<String>()
    val note = MutableLiveData<String?>()

    val formulatedRequest = MutableLiveData<Request>()
    private val finalRequests = MutableLiveData<List<Request>>()

    init {
        requesterNumber.addSource(activeAccount) { setRequesterNumber(it) }
    }

    fun setAmount(a: String?) = amount.postValue(a)

    fun setActiveAccount(account: Account) = activeAccount.postValue(account)

    private fun setRequesterNumber(a: Account?) {
        requesterNumber.postValue(a?.accountNo ?: "")
    }

    fun setRequesterNumber(number: String) {
        activeAccount.value?.let { it.accountNo = number }
        requesterNumber.postValue(number)
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
            getString(R.string.request_error_recipient)
    }

    fun accountError(): String? = if (activeAccount.value != null) null else getString(R.string.accounts_error_noselect)

    fun isValidAccount(): Boolean = activeAccount.value!!.name != PLACEHOLDER

    fun requesterAcctNoError(): String? = if (!requesterNumber.value.isNullOrEmpty()) null else getString(R.string.requester_number_fielderror)

    fun validNote(): Boolean = !note.value.isNullOrEmpty()

    //TODO validate that this works from schedule
    fun setSchedule(s: Schedule) {
        schedule.postValue(s)
        setAmount(s.amount)

        viewModelScope.launch {
            val contacts = contactRepo.getContacts(s.recipient_ids.split(",").toTypedArray())
            requestees.postValue(contacts)
        }
    }

    fun createRequest() {
        saveContacts()
        
        activeAccount.value?.let {
            val request = Request(amount.value, note.value, requesterNumber.value, it.institutionId!!)
            formulatedRequest.value = request
        }
    }

    fun saveRequest() {
        if (formulatedRequest.value != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val r = Request(formulatedRequest.value!!, requestee.value, getApplication())
                repo.insert(r)

                finalRequests.postValue(listOf(r))
            }
        }
    }

    private fun saveContacts() {
        requestee.value?.let { contact ->
            viewModelScope.launch {
                if (!contact.accountNumber.isNullOrEmpty()) {
                    contact.lastUsedTimestamp = DateUtils.now()
                    contactRepo.save(contact)
                }
            }
        }
    }

    override fun reset() {
        super.reset()
        setAmount(null)
        setNote(null)
        requestee.value = null
    }
}