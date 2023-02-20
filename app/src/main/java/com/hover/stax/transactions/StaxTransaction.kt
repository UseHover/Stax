/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.transactions

import android.content.Context
import android.content.Intent
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.HoverParameters
import com.hover.sdk.transactions.Transaction
import com.hover.sdk.transactions.TransactionContract
import com.hover.stax.R
import com.hover.stax.contacts.StaxContact
import com.hover.stax.domain.model.ACCOUNT_ID
import com.hover.stax.paybill.BUSINESS_NO
import com.hover.stax.utils.DateUtils.now
import com.hover.stax.utils.Utils
import java.util.HashMap
import timber.log.Timber

@Entity(tableName = "stax_transactions", indices = [Index(value = ["uuid"], unique = true)])
data class StaxTransaction(

    @ColumnInfo(name = "uuid")
    val uuid: String,

    @ColumnInfo(name = "action_id")
    val action_id: String,

    @ColumnInfo(name = "environment", defaultValue = "0")
    val environment: Int,

    @ColumnInfo(name = "transaction_type")
    val transaction_type: String,

    @ColumnInfo(name = "channel_id")
    val channel_id: Int,

    @ColumnInfo(name = "status", defaultValue = Transaction.PENDING)
    var status: String,

    @ColumnInfo(name = "category", defaultValue = "started")
    var category: String,

    @ColumnInfo(name = "initiated_at", defaultValue = "CURRENT_TIMESTAMP")
    val initiated_at: Long,

    @ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
    var updated_at: Long,
) : Comparable<StaxTransaction>, TransactionUiDelegate {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo(name = "description")
    lateinit var description: String

    @ColumnInfo(name = "account_id")
    var accountId: Int? = null

    @ColumnInfo(name = "recipient_id")
    var counterparty_id: String? = null

    @ColumnInfo(name = "amount")
    var amount: Double? = null

    @ColumnInfo(name = "fee")
    var fee: Double? = null

    @ColumnInfo(name = "confirm_code")
    var confirm_code: String? = null

    @ColumnInfo(name = "balance")
    var balance: String? = null

    @ColumnInfo(name = "note")
    var note: String? = null

    @ColumnInfo(name = "counterparty")
    var counterpartyNo: String? = null

    // Unused, remove.
    @ColumnInfo(name = "account_name")
    var accountName: String? = null

    constructor(data: Intent, action: HoverAction, contact: StaxContact?, context: Context) : this(
        data.getStringExtra(TransactionContract.COLUMN_UUID) ?: "",
        data.getStringExtra(TransactionContract.COLUMN_ACTION_ID)!!,
        data.getIntExtra(TransactionContract.COLUMN_ENVIRONMENT, 0),
        data.getStringExtra(TransactionContract.COLUMN_TYPE)!!,
        data.getIntExtra(TransactionContract.COLUMN_CHANNEL_ID, -1),
        data.getStringExtra(TransactionContract.COLUMN_STATUS)!!,
        if (hasExtra(data, TransactionContract.COLUMN_CATEGORY)) data.getStringExtra(TransactionContract.COLUMN_CATEGORY)!! else "started",
        data.getLongExtra(TransactionContract.COLUMN_REQUEST_TIMESTAMP, now()),
        data.getLongExtra(TransactionContract.COLUMN_UPDATE_TIMESTAMP, now()),
    ) {
        counterparty_id = contact?.id
        counterpartyNo = getCounterPartyNo(data, contact)
        parseExtras(data.getSerializableExtra(TransactionContract.COLUMN_INPUT_EXTRAS) as HashMap<String, String>?)
        parseExtras(data.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as HashMap<String, String>?)
        description = generateDescription(action, contact, context)
        Timber.v("creating transaction with uuid: %s", uuid)
    }

    private fun getCounterPartyNo(intent: Intent, contact: StaxContact?): String? {
        return when {
            contact != null -> contact.accountNumber
            intent.hasExtra(HoverAction.ACCOUNT_KEY) -> intent.getStringExtra(HoverAction.ACCOUNT_KEY)
            intent.hasExtra(BUSINESS_NO) -> intent.getStringExtra(BUSINESS_NO)
            else -> null
        }
    }

    fun update(data: Intent, contact: StaxContact) {
        status = data.getStringExtra(TransactionContract.COLUMN_STATUS)!!
        if (hasExtra(data, TransactionContract.COLUMN_CATEGORY))
            category = data.getStringExtra(TransactionContract.COLUMN_CATEGORY)!!
        parseExtras(data.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as HashMap<String, String>?)
        if (counterparty_id == null) counterparty_id = contact.id
        updated_at = data.getLongExtra(TransactionContract.COLUMN_UPDATE_TIMESTAMP, initiated_at)
    }

    private fun parseExtras(extras: HashMap<String, String>?) {
        if (extras == null) return
        Timber.e("Extras %s", extras.keys)
        if (extras.containsKey(HoverAction.AMOUNT_KEY)) amount = Utils.amountToDouble(extras[HoverAction.AMOUNT_KEY]!!)
        if (extras.containsKey(FEE_KEY)) fee = Utils.amountToDouble(extras[FEE_KEY]!!)
        if (extras.containsKey(CONFIRM_CODE_KEY)) confirm_code = extras[CONFIRM_CODE_KEY]
        if (extras.containsKey(HoverAction.BALANCE)) balance = extras[HoverAction.BALANCE]
        if (extras.containsKey(ACCOUNT_ID)) accountId = extras[ACCOUNT_ID]!!.toInt()
        if (extras.containsKey(HoverAction.NOTE_KEY)) note = extras[HoverAction.NOTE_KEY]
        if (extras.containsKey(HoverAction.ACCOUNT_KEY)) counterpartyNo = extras[HoverAction.ACCOUNT_KEY]
        else if (extras.containsKey(BUSINESS_NO)) counterpartyNo = extras[BUSINESS_NO]
    }

    private fun generateDescription(action: HoverAction, contact: StaxContact?, c: Context): String {
        if (isRecorded) return c.getString(R.string.descrip_recorded, action.from_institution_name)
        val amountStr = Utils.formatAmount(amount)
        return when (transaction_type) {
            HoverAction.RECEIVE -> c.getString(R.string.descrip_transfer_received, contact!!.shortName())
            HoverAction.BALANCE -> c.getString(R.string.descrip_balance, action.from_institution_name)
            HoverAction.AIRTIME -> c.getString(
                R.string.descrip_airtime_sent, amountStr,
                if (contact == null) c.getString(R.string.self_choice) else contact.shortName()
            )
            HoverAction.P2P -> c.getString(R.string.descrip_transfer_sent, amountStr, contact!!.shortName())
            HoverAction.ME2ME -> c.getString(R.string.descrip_transfer_sent, amountStr, action.to_institution_name)
            HoverAction.BILL -> c.getString(R.string.descrip_bill_paid, amountStr, counterpartyNo, action.to_institution_name)
            HoverAction.MERCHANT -> c.getString(R.string.descrip_merchant_paid, amountStr, counterpartyNo)
            else -> "Other"
        }
    }

    val canRetry: Boolean
        get() = isRecorded || (
            (
                transaction_type == HoverAction.P2P || transaction_type == HoverAction.AIRTIME ||
                    transaction_type == HoverAction.BALANCE
                ) && isFailed
            )

    val isFailed: Boolean
        get() = status == Transaction.FAILED

    val isPending: Boolean
        get() = status == Transaction.PENDING

    val isSuccessful: Boolean
        get() = status == Transaction.SUCCEEDED

    val isBalanceType: Boolean
        get() = transaction_type == HoverAction.BALANCE

    val isRecorded: Boolean
        get() = environment == HoverParameters.MANUAL_ENV

    override val transaction: StaxTransaction get() = this

    fun toString(context: Context): String {
        var str = HoverAction.getHumanFriendlyType(context, transaction_type)
        str = String.format("%s%s", str.substring(0, 1).uppercase(), str.substring(1))
        return str
    }

    companion object {
        const val CONFIRM_CODE_KEY = "confirmCode"
        const val FEE_KEY = "fee"
        const val MMI_ERROR = "mmi-error"
        const val PIN_ERROR = "pin-error"
        const val BALANCE_ERROR = "balance-error"
        const val UNREGISTERED_ERROR = "unregistered-error"
        const val INVALID_ENTRY_ERROR = "invalid-entry"
        const val NO_RESPONSE_ERROR = "no-response"
        const val INCOMPLETE_ERROR = "incomplete"

        fun hasExtra(data: Intent, key: String): Boolean {
            return data.hasExtra(key) && data.getStringExtra(key) != null
        }
    }

    override fun compareTo(other: StaxTransaction): Int = uuid.compareTo(other.uuid)
}