package com.hover.stax.transactions

import android.app.Application
import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.api.Hover.getSMSMessageByUUID
import com.hover.stax.contacts.StaxContact
import com.hover.stax.database.DatabaseRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionDetailsViewModel(val repo: DatabaseRepo, val application: Application) : ViewModel() {

    val transaction = MutableLiveData<StaxTransaction>()
    val messages = MediatorLiveData<List<UssdCallResponse>>()
    var action: LiveData<HoverAction> = MutableLiveData()
    var contact: LiveData<StaxContact> = MutableLiveData()
    var sms: LiveData<List<UssdCallResponse>> = MutableLiveData()

    init {
        action = Transformations.switchMap(transaction) { getLiveAction(it) }
        contact = Transformations.switchMap(transaction) { getLiveContact(it) }

        messages.apply {
            addSource(transaction) { loadMessages(it) }
            addSource(action) { loadMessages(it) }
        }

        sms = Transformations.map(transaction) { loadSms(it) }
    }

    private fun getLiveAction(txn: StaxTransaction?): LiveData<HoverAction>? = if (txn != null)
        repo.getLiveAction(txn.action_id)
    else null

    private fun getLiveContact(txn: StaxTransaction?): LiveData<StaxContact>? = if (txn != null)
        repo.getLiveContact(txn.counterparty_id)
    else null

    fun setTransaction(uuid: String) = viewModelScope.launch(Dispatchers.IO) {
        transaction.postValue(repo.getTransaction(uuid))
    }

    private fun loadMessages(txn: StaxTransaction?) {
        if (action.value != null && txn != null) loadMessages(txn, action.value!!)
    }

    private fun loadMessages(a: HoverAction?) {
        if (transaction.value != null && a != null) loadMessages(transaction.value!!, a)
    }

    private fun loadMessages(txn: StaxTransaction, a: HoverAction) {
        messages.value = UssdCallResponse.generateConvo(Hover.getTransaction(txn.uuid, application), a)
    }

    private fun loadSms(txn: StaxTransaction?): List<UssdCallResponse>? {
        if (txn == null) return null

        val t = Hover.getTransaction(txn.uuid, application)
        if (t.smsHits == null) return null

        val smses = ArrayList<UssdCallResponse>()

        for (i in 0 until t.smsHits.length()) {
            val sms = getSMSMessageByUUID(t.smsHits.optString(i), application)
            smses.add(UssdCallResponse(null, sms.msg))
        }

        return smses
    }
}