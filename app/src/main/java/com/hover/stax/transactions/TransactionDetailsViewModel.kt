package com.hover.stax.transactions

import android.app.Application
import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.api.Hover.getSMSMessageByUUID
import com.hover.stax.channels.Channel
import com.hover.stax.contacts.StaxContact
import com.hover.stax.database.DatabaseRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONArray
import timber.log.Timber

class TransactionDetailsViewModel(val repo: DatabaseRepo, val application: Application) : ViewModel() {

    val transaction = MutableLiveData<StaxTransaction>()
    val messages = MediatorLiveData<List<UssdCallResponse>>()
    var action: LiveData<HoverAction> = MutableLiveData()
    var contact: LiveData<StaxContact> = MutableLiveData()
    var sms: LiveData<List<UssdCallResponse>> = MutableLiveData()

    val actionAndChannelPair: MediatorLiveData<Pair<HoverAction, Channel>> = MediatorLiveData()

    init {
        action = Transformations.switchMap(transaction) { getLiveAction(it) }
        contact = Transformations.switchMap(transaction) { getLiveContact(it) }
        actionAndChannelPair.addSource(transaction) {setActionAndChannel(it)}

        messages.apply {
            addSource(transaction) { loadMessages(it) }
            addSource(action) { loadMessages(it) }
        }

        sms = Transformations.map(transaction) { it?.let { loadSms(it) } }
    }

    private fun getLiveAction(txn: StaxTransaction?): LiveData<HoverAction>? = if (txn != null)
        repo.getLiveAction(txn.action_id)
    else null

    private fun getLiveContact(txn: StaxTransaction?): LiveData<StaxContact>? = if (txn != null)
        repo.getLiveContact(txn.counterparty_id)
    else null

    fun setTransaction(uuid: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.getTransactionAsync(uuid).collect { transaction.postValue(it) }
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

    private fun loadSms(txn: StaxTransaction): List<UssdCallResponse>? {
        val t = Hover.getTransaction(txn.uuid, application)
        return generateSmsConvo(if (t.smsHits != null && t.smsHits.length() > 0) t.smsHits else t.smsMisses)
    }

    private fun generateSmsConvo(smsArr: JSONArray): ArrayList<UssdCallResponse> {
        val smses = ArrayList<UssdCallResponse>()
        for (i in 0 until smsArr.length()) {
            val sms = getSMSMessageByUUID(smsArr.optString(i), application)
            Timber.e(sms.uuid)
            smses.add(UssdCallResponse(null, sms.msg))
        }
        return smses
    }

    private fun setActionAndChannel(transaction: StaxTransaction){
        viewModelScope.launch(Dispatchers.IO) {
            val action: HoverAction = repo.getAction(transaction.action_id)!!
            val channel: Channel = repo.getChannel(transaction.channel_id)!!
            actionAndChannelPair.postValue(Pair(action, channel))
        }
    }
}