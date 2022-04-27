package com.hover.stax.requests

import androidx.lifecycle.*
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.AccountRepo
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelRepo
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.contacts.StaxContact
import com.hover.stax.schedules.ScheduleRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RequestDetailViewModel(val repo: AccountRepo, val requestRepo: RequestRepo, val contactRepo: ContactRepo) : ViewModel() {

    val request: MutableLiveData<Request> = MutableLiveData()
    var account: LiveData<Account> = MutableLiveData()
    var recipients: LiveData<List<StaxContact>> = MutableLiveData()

    init {
        account = Transformations.switchMap(request) { r -> r?.let { loadAccount(r) } }
        recipients = Transformations.switchMap(request) { r -> r?.let { loadRecipients(r) } }
    }

    fun loadAccount(r: Request): LiveData<Account> {
        return repo.getLiveAccount(r.requester_account_id)
    }

    fun setRequest(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        request.postValue(requestRepo.getRequest(id))
    }

    private fun loadRecipients(r: Request): LiveData<List<StaxContact>> {
        return contactRepo.getLiveContacts(r.requestee_ids.split(",").toTypedArray())
    }

    fun deleteRequest() = viewModelScope.launch(Dispatchers.IO) {
        requestRepo.delete(request.value)
    }

}