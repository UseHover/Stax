package com.hover.stax.transactionDetails

import android.app.Application
import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.api.Hover.getSMSMessageByUUID
import com.hover.sdk.transactions.Transaction
import com.hover.stax.domain.model.Account
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.data.local.bonus.BonusRepo
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.contacts.StaxContact
import com.hover.stax.data.local.parser.ParserRepo
import com.hover.stax.merchants.Merchant
import com.hover.stax.merchants.MerchantRepo
import com.hover.stax.transactions.StaxTransaction
import com.hover.stax.transactions.TransactionRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import timber.log.Timber
import kotlin.math.floor

class TransactionDetailsViewModel(
    application: Application, val repo: TransactionRepo, val actionRepo: ActionRepo, val contactRepo: ContactRepo, val accountRepo: AccountRepo,
    private val bonusRepo: BonusRepo, private val parserRepo: ParserRepo, private val merchantRepo: MerchantRepo
) : AndroidViewModel(application) {

    val transaction = MutableLiveData<StaxTransaction>()
    var account: LiveData<Account> = MutableLiveData()
    var action: LiveData<HoverAction> = MutableLiveData()

    var contact: LiveData<StaxContact> = MutableLiveData()
    var merchant: LiveData<Merchant> = MutableLiveData()

    var hoverTransaction = MutableLiveData<Transaction>()
    val messages = MediatorLiveData<List<UssdCallResponse>>()
    var sms: LiveData<List<UssdCallResponse>> = MutableLiveData()
    val isExpectingSMS: MediatorLiveData<Boolean> = MediatorLiveData<Boolean>().also { it.value = false }
    var bonusAmt: MediatorLiveData<Int> = MediatorLiveData()

    init {
        account = Transformations.switchMap(transaction) { getLiveAccount(it) }
        action = Transformations.switchMap(transaction) { getLiveAction(it) }
        contact = Transformations.switchMap(transaction) { getLiveContact(it) }
        merchant = Transformations.switchMap(transaction) { getLiveMerchant(it) }
        bonusAmt.addSource(transaction, this::getBonusAmount)

        messages.apply {
            addSource(transaction) { loadMessages(it) }
            addSource(action) { loadMessages(it) }
        }

        sms = Transformations.map(transaction) { it?.let { loadSms(it) } }
        isExpectingSMS.addSource(transaction, this::setExpectingSMS)
    }

    private fun getLiveAccount(txn: StaxTransaction?): LiveData<Account>? = if (txn != null)
        txn.accountId?.let { accountRepo.getLiveAccount(it) }
    else null

    private fun getLiveAction(txn: StaxTransaction?): LiveData<HoverAction>? = if (txn != null)
        actionRepo.getLiveAction(txn.action_id)
    else null

    private fun getLiveContact(txn: StaxTransaction?): LiveData<StaxContact>? = if (txn != null)
        contactRepo.getLiveContact(txn.counterparty_id)
    else null

    private fun getLiveMerchant(txn: StaxTransaction?): LiveData<Merchant?>? =
            if (txn != null && txn.transaction_type == HoverAction.MERCHANT && txn.counterpartyNo != null)
        merchantRepo.getLiveMatching(txn.counterpartyNo!!, txn.channel_id)
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
        val t = Hover.getTransaction(txn.uuid, getApplication())
        hoverTransaction.value = t
        messages.value = UssdCallResponse.generateConvo(t, a)
    }

    private fun loadSms(txn: StaxTransaction): List<UssdCallResponse> {
        val t = Hover.getTransaction(txn.uuid, getApplication())
        hoverTransaction.value = t
        return generateSmsConvo(
            if (t.smsHits != null && t.smsHits.length() > 0) t.smsHits
            else t.smsMisses
        )
    }

    private fun generateSmsConvo(smsArr: JSONArray): ArrayList<UssdCallResponse> {
        val smses = ArrayList<UssdCallResponse>()
        for (i in 0 until smsArr.length()) {
            val sms = getSMSMessageByUUID(smsArr.optString(i), getApplication())
            Timber.e(sms.uuid)
            smses.add(
                UssdCallResponse(
                    null,
                    sms.msg
                )
            )
        }
        return smses
    }

    fun wrapExtras(): HashMap<String, String> {
        val extras = HashMap<String, String>()
        if (transaction.value?.amount != null) extras[HoverAction.AMOUNT_KEY] = transaction.value!!.amount.toString()
        if (contact.value?.accountNumber != null) extras[HoverAction.PHONE_KEY] = contact.value!!.accountNumber
        if (contact.value?.accountNumber != null) extras[HoverAction.ACCOUNT_KEY] = contact.value!!.accountNumber
        if (transaction.value?.counterparty_id != null) extras[StaxContact.ID_KEY] = transaction.value!!.counterparty_id!!
        if (transaction.value?.note != null) extras[HoverAction.NOTE_KEY] = transaction.value!!.note!!
        Timber.e("Extras %s", extras.keys)
        return extras
    }

    private fun setExpectingSMS(transaction: StaxTransaction?) = viewModelScope.launch(Dispatchers.IO) {
        transaction?.let {
            val hasSMSParser = parserRepo.hasSMSParser(transaction.action_id)
            if (transaction.isPending) isExpectingSMS.postValue(hasSMSParser)
        }
    }

    private fun getBonusAmount(staxTransaction: StaxTransaction?) = viewModelScope.launch(Dispatchers.IO) {
        staxTransaction?.let {
            val bonus = bonusRepo.getBonusByPurchaseChannel(staxTransaction.channel_id)

            if (bonus != null && staxTransaction.amount != null) {
                val bonusAmount = floor(bonus.bonusPercent.times(staxTransaction.amount!!))
                bonusAmt.postValue(bonusAmount.toInt())
            } else
                bonusAmt.postValue(0)
        }
    }
}