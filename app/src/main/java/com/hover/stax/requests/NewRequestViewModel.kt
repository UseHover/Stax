package com.hover.stax.requests

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.hover.stax.channels.Channel
import com.hover.stax.contacts.StaxContact
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.transfers.AbstractFormViewModel
import java.util.*
import kotlin.collections.ArrayList

class NewRequestViewModel(application: Application, databaseRepo: DatabaseRepo): AbstractFormViewModel(application, databaseRepo) {

    val activeChannel = MediatorLiveData<Channel>()
    val amount = MutableLiveData<String>()
    val requestees = MutableLiveData<List<StaxContact>>(Collections.singletonList(StaxContact("")))
    val requesterNumber = MediatorLiveData<String>()
    val note = MutableLiveData<String>()
    val formulatedRequest = MutableLiveData<Request>()
    val finalRequests = MutableLiveData<List<Request>>()

    init {
        requesterNumber.addSource(activeChannel, this::setRequesterNumber)
    }

    fun setAmount(a: String) = amount.postValue(a)

    fun setActiveChannel(c: Channel) = activeChannel.postValue(c)

    fun setRequesterNumber(c: Channel) = requesterNumber.postValue(c.accountNo)

    fun setRequesterNumber(number: String) {
        requesterNumber.postValue(number)
        activeChannel.value?.let { it.accountNo = number }
    }

    fun onUpdate(pos: Int, contact: StaxContact){
        val cs = ArrayList<StaxContact>()

        if(!requestees.value.isNullOrEmpty()){
            cs.addAll(requestees.value!!)
        } else {
            cs.add(pos, contact)
        }

        requestees.postValue(cs)
    }

    fun addRecipient(contact: StaxContact){
        val rList = arrayListOf<StaxContact>()

        if(!requestees.value.isNullOrEmpty())
            rList.addAll(requestees.value!!)

        rList.add(contact)
        requestees.postValue(rList)
    }
}