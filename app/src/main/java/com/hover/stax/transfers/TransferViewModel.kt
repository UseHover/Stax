package com.hover.stax.transfers

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.contacts.PhoneHelper
import com.hover.stax.contacts.StaxContact
import com.hover.stax.data.local.bonus.BonusRepo
import com.hover.stax.domain.model.BonusList
import com.hover.stax.domain.use_case.bonus.GetBonusesUseCase
import com.hover.stax.requests.Request
import com.hover.stax.requests.RequestRepo
import com.hover.stax.schedules.ScheduleRepo
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.DateUtils
import com.hover.stax.utils.Utils
import com.yariksoffice.lingver.Lingver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

const val STAX_PREFIX = "stax_airtime_prefix"
private const val KE_PREFIX = "0"

class TransferViewModel(application: Application, private val getBonusesUseCase: GetBonusesUseCase, private val requestRepo: RequestRepo, contactRepo: ContactRepo, scheduleRepo: ScheduleRepo) : AbstractFormViewModel(application, contactRepo, scheduleRepo) {

    private val _bonusList = MutableStateFlow(BonusList())
    val bonusList = _bonusList.asStateFlow()

    val amount = MutableLiveData<String?>()
    val contact = MutableLiveData<StaxContact?>()
    val note = MutableLiveData<String?>()

    val isLoading = MutableLiveData(false)

    init {
    	collectBonusList()
    }

    fun setAmount(a: String?) = amount.postValue(a)

    fun setContact(contactId: String) = viewModelScope.launch(Dispatchers.IO) {
        contact.postValue(contactRepo.getContact(contactId))
    }

    fun setContact(sc: StaxContact?) = sc?.let {
        contact.postValue(it)
    }

    fun setRecipientNumber(str: String) {
        if (contact.value != null && contact.value.toString() == str) return
        contact.value = if (str.isEmpty()) StaxContact() else StaxContact(str)
    }

    private fun setRecipientSmartly(r: Request?, countryAlpha2: String?) =
        viewModelScope.launch(Dispatchers.IO) {
            r?.let {
                try {
                    val formattedPhone = PhoneHelper.getNationalSignificantNumber(
                        it.requester_number!!,
                        countryAlpha2 ?: Lingver.getInstance().getLocale().country
                    )
                    val sc = contactRepo.getContactByPhone(formattedPhone)
                    contact.postValue(sc ?: StaxContact(r.requester_number))
                    isLoading.postValue(false)
                } catch (e: NumberFormatException) {
                    AnalyticsUtil.logErrorAndReportToFirebase(
                        TransferViewModel::class.java.simpleName, e.message!!, e
                    )
                }
            }
        }

    private fun setNote(n: String?) = note.postValue(n)

    fun amountErrors(a: HoverAction?): String? {
        val regex = a?.getStepByVar(HoverAction.AMOUNT_KEY)?.optString("valid_response_regex")
        Timber.i("Custom regex is $regex")
        return if (!regex.isNullOrEmpty() && amount.value!!.matches(regex.toRegex())) null
        else if (!amount.value.isNullOrEmpty() && amount.value!!.matches("[\\d.]+".toRegex()) && !amount.value!!.matches("[0]+".toRegex())) null
        else getString(R.string.amount_fielderror)
    }

    fun recipientErrors(a: HoverAction?): String? {
        return when {
            (a != null && a.requiresRecipient() && (contact.value == null || contact.value?.accountNumber.isNullOrEmpty())) -> getString(if (a.isPhoneBased) R.string.transfer_error_recipient_phone else R.string.transfer_error_recipient_account)
            else -> null
        }
    }

    fun wrapExtras(isBonusAirtime: Boolean = false): HashMap<String, String> {
        val extras: HashMap<String, String> = hashMapOf()
        if (amount.value != null) extras[HoverAction.AMOUNT_KEY] = amount.value!!
        if (contact.value != null && contact.value?.accountNumber != null) {
            extras[StaxContact.ID_KEY] = contact.value!!.id
            extras[HoverAction.PHONE_KEY] = contact.value!!.accountNumber
            extras[HoverAction.ACCOUNT_KEY] = if (isBonusAirtime) staxPrefix.plus(KE_PREFIX).plus(PhoneHelper.getNationalSignificantNumber(contact.value!!.accountNumber, "KE")) else
                contact.value!!.accountNumber
        }
        if (note.value != null) extras[HoverAction.NOTE_KEY] = note.value!!
        return extras
    }

    private val staxPrefix get() = Utils.getString(STAX_PREFIX, getApplication())

    fun load(encryptedString: String) = viewModelScope.launch {
        isLoading.postValue(true)
        val r: Request? = requestRepo.decrypt(encryptedString, getApplication())
        Timber.v("Loaded request %s", r)
        r?.let {
            setRecipientSmartly(r, r.requester_country_alpha2)
            setAmount(r.amount)
            setNote(r.note)
            AnalyticsUtil.logAnalyticsEvent(getString(R.string.loaded_request_link), getApplication())
        }
    }

    fun saveContact() {
        contact.value?.let { sc ->
            viewModelScope.launch {
                sc.lastUsedTimestamp = DateUtils.now()
                contactRepo.save(sc)
            }
        }
    }

    private fun collectBonusList() = viewModelScope.launch(Dispatchers.IO) {
        getBonusesUseCase.bonusList.collect { items ->
            _bonusList.update { _bonusList.value.copy(bonuses = items) }
        }
    }

    override fun reset() {
        super.reset()
        amount.value = null
        contact.value = null
        note.value = null
    }
}