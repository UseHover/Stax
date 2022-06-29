package com.hover.stax.requests

import androidx.lifecycle.*
import com.hover.stax.domain.model.Account
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.contacts.StaxContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RequestDetailViewModel(val repo: AccountRepo, private val requestRepo: RequestRepo, val contactRepo: ContactRepo) : ViewModel() {

    val request: MutableLiveData<Request> = MutableLiveData()
    var account: LiveData<Account> = MutableLiveData()
    var recipients: LiveData<List<StaxContact>> = MutableLiveData()

    init {
        account = Transformations.switchMap(request) { r -> r?.let { loadAccount(r) } }
        recipients = Transformations.switchMap(request) { r -> r?.let { loadRecipients(r) } }
    }

    private fun loadAccount(r: Request): LiveData<Account> {
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