package com.hover.stax.transactions

import android.app.Application
import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.api.Hover.getSMSMessageByUUID
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.AccountRepo
import com.hover.stax.actions.ActionRepo
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.contacts.StaxContact
import com.hover.stax.schedules.ScheduleRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import timber.log.Timber

class TransactionDetailsViewModel(application: Application, val repo: TransactionRepo, val actionRepo: ActionRepo, val contactRepo: ContactRepo, val accountRepo: AccountRepo) : AndroidViewModel(application) {

    val transaction = MutableLiveData<StaxTransaction>()
    var account: LiveData<Account> = MutableLiveData()
    var contact: LiveData<StaxContact> = MutableLiveData()
    var action: LiveData<HoverAction> = MutableLiveData()

    val messages = MediatorLiveData<List<UssdCallResponse>>()
    var sms: LiveData<List<UssdCallResponse>> = MutableLiveData()

    init {
        account = Transformations.switchMap(transaction) { getLiveAccount(it) }
        action = Transformations.switchMap(transaction) { getLiveAction(it) }
        contact = Transformations.switchMap(transaction) { getLiveContact(it) }

        messages.apply {
            addSource(transaction) { loadMessages(it) }
            addSource(action) { loadMessages(it) }
        }

        sms = Transformations.map(transaction) { it?.let { loadSms(it) } }
    }

    private fun getLiveAccount(txn: StaxTransaction?): LiveData<Account>? = if (txn != null)
        accountRepo.getLiveAccount(txn.accountId)
    else null

    private fun getLiveAction(txn: StaxTransaction?): LiveData<HoverAction>? = if (txn != null)
        actionRepo.getLiveAction(txn.action_id)
    else null

    private fun getLiveContact(txn: StaxTransaction?): LiveData<StaxContact>? = if (txn != null)
        contactRepo.getLiveContact(txn.counterparty_id)
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
        messages.value = UssdCallResponse.generateConvo(Hover.getTransaction(txn.uuid, getApplication()), a)
    }

    private fun loadSms(txn: StaxTransaction): List<UssdCallResponse>? {
        val t = Hover.getTransaction(txn.uuid, getApplication())
        return generateSmsConvo(if (t.smsHits != null && t.smsHits.length() > 0) t.smsHits else t.smsMisses)
    }

    private fun generateSmsConvo(smsArr: JSONArray): ArrayList<UssdCallResponse> {
        val smses = ArrayList<UssdCallResponse>()
        for (i in 0 until smsArr.length()) {
            val sms = getSMSMessageByUUID(smsArr.optString(i), getApplication())
            Timber.e(sms.uuid)
            smses.add(UssdCallResponse(null, sms.msg))
        }
        return smses
    }

    fun wrapExtras(): HashMap<String, String>? {
        val extras = HashMap<String, String>()
        if (transaction.value?.amount != null) extras[HoverAction.AMOUNT_KEY] = transaction.value!!.amount.toString()
        if (contact.value?.accountNumber != null) extras[HoverAction.PHONE_KEY] = contact.value!!.accountNumber
        if (contact.value?.accountNumber != null) extras[HoverAction.ACCOUNT_KEY] = contact.value!!.accountNumber
        if (transaction.value?.counterparty_id != null) extras[StaxContact.ID_KEY] = transaction.value!!.counterparty_id
        if (transaction.value?.note != null) extras[HoverAction.NOTE_KEY] = transaction.value!!.note
        Timber.e("Extras %s", extras.keys)
        return extras
    }
}