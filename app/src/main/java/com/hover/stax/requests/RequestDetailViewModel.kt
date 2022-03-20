package com.hover.stax.requests

import androidx.lifecycle.*
import com.hover.stax.channels.Channel
import com.hover.stax.contacts.StaxContact
import com.hover.stax.database.DatabaseRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RequestDetailViewModel(val repo: DatabaseRepo) : ViewModel() {

    val request: MutableLiveData<Request> = MutableLiveData()
    var channel: LiveData<Channel> = MutableLiveData()
    var recipients: LiveData<List<StaxContact>> = MutableLiveData()

    init {
        channel = Transformations.switchMap(request) { r -> repo.getLiveChannel(r.requester_institution_id) }
        recipients = Transformations.switchMap(request) { r -> loadRecipients(r) }
    }

    fun setRequest(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        request.postValue(repo.getRequest(id))
    }

    private fun loadRecipients(r: Request): LiveData<List<StaxContact>> {
        return repo.getLiveContacts(r.requestee_ids.split(",").toTypedArray())
    }

    fun deleteRequest() = viewModelScope.launch(Dispatchers.IO) {
        repo.delete(request.value)
    }

}