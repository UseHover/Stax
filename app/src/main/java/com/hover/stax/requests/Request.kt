package com.hover.stax.requests

import android.content.Context

import android.text.TextUtils
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.hover.stax.R
import com.hover.stax.domain.model.Account
import com.hover.stax.channels.Channel
import com.hover.stax.contacts.PhoneHelper
import com.hover.stax.contacts.StaxContact
import com.hover.stax.utils.DateUtils.now
import com.hover.stax.utils.Utils.formatAmount
import com.hover.stax.utils.paymentLinkCryptography.Base64
import com.hover.stax.utils.paymentLinkCryptography.Encryption
import com.yariksoffice.lingver.Lingver
import timber.log.Timber

@Entity(tableName = "requests")
class Request {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "description")
    var description: String? = null

    @ColumnInfo(name = "requestee_ids")
    var requestee_ids: String = ""

    @ColumnInfo(name = "amount")
    var amount: String? = null

    @ColumnInfo(name = "requester_institution_id", defaultValue = "0")
    var requester_institution_id = 0

    @ColumnInfo(name = "requester_number")
    var requester_number: String? = null

    @ColumnInfo(name = "requester_country_alpha2")
    var requester_country_alpha2: String? = null

    @ColumnInfo(name = "note")
    var note: String? = null

    @ColumnInfo(name = "message")
    var message: String? = null

    @ColumnInfo(name = "matched_transaction_uuid")
    var matched_transaction_uuid: String? = null

    @ColumnInfo(name = "requester_account_id")
    var requester_account_id: Int? = null

    @ColumnInfo(name = "date_sent", defaultValue = "CURRENT_TIMESTAMP")
    var date_sent: Long = 0

    constructor()

    @Ignore
    constructor(amount: String?, note: String?, requester_number: String? ,  requester_institution_id:Int ) {
        this.amount = amount
        this.note = note
        this.requester_number = requester_number
        this.requester_institution_id = requester_institution_id
        date_sent = now()
    }

    @Ignore
    constructor(r: Request, requestee: StaxContact?, c: Context) {
        amount = r.amount
        note = r.note
        requestee?.let { requestee_ids = it.id }
        requester_number = r.requester_number!!.replace(" ".toRegex(), "")
        requester_institution_id = r.requester_institution_id
        date_sent = r.date_sent
        description = getDescription(requestee, c)
    }

    @Ignore
    constructor(paymentLink: String) {
        Timber.v("Creating request from link: %s", paymentLink)
        val splitString = paymentLink.split(PAYMENT_LINK_SEPERATOR).toTypedArray()
        amount = if (splitString[0] == "0.00") "" else formatAmount(splitString[0])
        requester_institution_id = splitString[1].toInt()
        requester_number = splitString[2]
    }


    fun hasRequesterInfo(): Boolean {
        return requester_number != null && requester_number!!.isNotEmpty()
    }

    private fun getDescription(contact: StaxContact?, c: Context): String {
        return c.getString(R.string.descrip_request, contact!!.shortName() ?: "")
    }

    fun generateRecipientString(contacts: List<StaxContact>): String {
        val phones = StringBuilder()
        for (r in contacts.indices) {
            if (phones.isNotEmpty()) phones.append(",")
            contacts[r].let { phones.append(it.accountNumber) }
        }
        return phones.toString()
    }

    fun generateWhatsappRecipientString(contacts: List<StaxContact>, account: Account?): String {
        val phones = StringBuilder()
        if(contacts.isNotEmpty()) {
            for (r in contacts.indices) {
                if (phones.isNotEmpty()) phones.append(",")
                contacts[r].let{
                    phones.append(
                        PhoneHelper.getInternationalNumberNoPlus(it.accountNumber,
                        account?.countryAlpha2 ?: Lingver.getInstance().getLocale().country))
                }
            }
        }
        return phones.toString()
    }

    fun generateMessage(c: Context): String {
        val amountString = if (amount != null) c.getString(R.string.sms_amount_detail,
            formatAmount(amount!!))
        else ""
        val noteString =
            if (note != null && !TextUtils.isEmpty(note)) c.getString(R.string.sms_note_detail,
                note)
            else " "
        val paymentLink = generateStaxLink(c)
        return c.getString(R.string.sms_request_template, amountString, noteString, paymentLink)
    }

    private fun generateStaxLink(c: Context): String {
        val amountNoFormat = if (!amount.isNullOrEmpty()) amount else "0.00"
        val requesterNumber = requester_number!!.replace("+", "")
        val params = c.getString(R.string.payment_url_end,
            amountNoFormat,
            requester_institution_id,
            requesterNumber,
            now())
        Timber.i("encrypting from: %s", params)
        val encryptedString = encryptBijective(params, c)
        Timber.i("link: %s", c.resources.getString(R.string.payment_root_url, encryptedString))
        return c.resources.getString(R.string.payment_root_url, encryptedString)
    }

    private fun encryptBijective(value: String, c: Context): String {
        val valueChar = value.toCharArray()
        val result = CharArray(value.toCharArray().size)
        for (i in valueChar.indices) {
            when (valueChar[i]) {
                '.' -> result[i] = bijectiveExt(c)[0]
                '-' -> result[i] = bijectiveExt(c)[1]
                ',' -> result[i] = bijectiveExt(c)[2]
                else -> {
                    val valueToInteger = valueChar[i].toString().toInt()
                    result[i] = bijectiveKey(c)[valueToInteger]
                }
            }
        }
        return String(result)
    }

    override fun toString(): String {
        return if (description == null) "Request from $requester_number" else description!!
    }

    companion object {
        const val PAYMENT_LINK_SEPERATOR = "-"
        private const val TAG = "Request"
        fun isShortLink(link: String): Boolean {
            return link.length <= 25
        }

        val encryptionSettings: Encryption.Builder
            get() = Encryption.Builder().setKeyLength(128).setKeyAlgorithm("AES")
                .setCharsetName("UTF8").setIterationCount(65536).setKey("ves€Z€xs€aBKgh")
                .setDigestAlgorithm("SHA1").setSalt("A secured salt").setBase64Mode(Base64.DEFAULT)
                .setAlgorithm("AES/CBC/PKCS5Padding").setSecureRandomAlgorithm("SHA1PRNG")
                .setSecretKeyType("PBKDF2WithHmacSHA1").setIv(byteArrayOf(29,
                    88,
                    -79,
                    -101,
                    -108,
                    -38,
                    -126,
                    90,
                    52,
                    101,
                    -35,
                    114,
                    12,
                    -48,
                    -66,
                    -30))

        fun decryptBijective(value: String, c: Context): String {
            val valueChar = value.toCharArray()
            val result = CharArray(value.toCharArray().size)
            for (i in valueChar.indices) {
                when (valueChar[i]) {
                    'g' -> result[i] = bijectiveExtReverse(c)[0]
                    'j' -> result[i] = bijectiveExtReverse(c)[1]
                    'r' -> result[i] = bijectiveExtReverse(c)[2]
                    else -> result[i] = Character.forDigit(getBijectiveIdx(valueChar[i], c), 10)
                }
            }
            return String(result)
        }

        private fun getBijectiveIdx(target: Char, c: Context): Int {
            val entries = bijectiveKey(c)
            var result = -1
            for (i in entries.indices) {
                if (entries[i] == target) {
                    result = i
                    break
                }
            }
            return result
        }

        private fun bijectiveKey(c: Context): CharArray {
            return c.getString(R.string.stax_link_bijective_key).toCharArray()
        }

        private fun bijectiveExt(c: Context): CharArray {
            return c.getString(R.string.stax_link_bijective_ext).toCharArray()
        }

        private fun bijectiveExtReverse(c: Context): CharArray {
            return c.getString(R.string.stax_link_bijective_ext_reverse).toCharArray()
        }
    }
}