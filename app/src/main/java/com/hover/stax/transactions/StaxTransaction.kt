package com.hover.stax.transactions

import android.content.Context
import com.hover.stax.utils.DateUtils.now
import com.hover.stax.utils.Utils.getAmount
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import android.content.Intent
import androidx.room.Entity
import androidx.room.Index
import com.hover.sdk.actions.HoverAction
import com.hover.stax.contacts.StaxContact
import com.hover.sdk.transactions.TransactionContract
import timber.log.Timber
import com.hover.stax.R
import com.hover.sdk.api.HoverParameters
import com.hover.sdk.transactions.Transaction
import com.hover.stax.accounts.ACCOUNT_ID
import com.hover.stax.utils.Utils
import java.util.HashMap

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

	@ColumnInfo(name = "account_name")
	var accountName: String? = null

	constructor(data: Intent, action: HoverAction, contact: StaxContact?, c: Context) : this(
		data.getStringExtra(TransactionContract.COLUMN_UUID)!!,
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
		parseExtras(data.getSerializableExtra(TransactionContract.COLUMN_INPUT_EXTRAS) as HashMap<String, String>?)
		parseExtras(data.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as HashMap<String, String>?)
		description = generateDescription(action, contact, c)
		Timber.v("creating transaction with uuid: %s", uuid)
	}

	fun update(data: Intent, action: HoverAction, contact: StaxContact, c: Context) {
		status = data.getStringExtra(TransactionContract.COLUMN_STATUS)!!
		if (hasExtra(data, TransactionContract.COLUMN_CATEGORY))
			category = data.getStringExtra(TransactionContract.COLUMN_CATEGORY)!!
		parseExtras(data.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as HashMap<String, String>?)
		if (counterparty_id == null) counterparty_id = contact.id
		description = generateDescription(action, contact, c)
		updated_at = data.getLongExtra(TransactionContract.COLUMN_UPDATE_TIMESTAMP, initiated_at)
	}

	private fun parseExtras(extras: HashMap<String, String>?) {
		if (extras == null) return
		Timber.e("Extras %s", extras.keys)
		if (extras.containsKey(HoverAction.AMOUNT_KEY)) amount = getAmount(extras[HoverAction.AMOUNT_KEY]!!)
		if (extras.containsKey(FEE_KEY)) fee = getAmount(extras[FEE_KEY]!!)
		if (extras.containsKey(CONFIRM_CODE_KEY)) confirm_code = extras[CONFIRM_CODE_KEY]
		if (extras.containsKey(HoverAction.BALANCE)) balance = extras[HoverAction.BALANCE]
		if (extras.containsKey(ACCOUNT_ID)) accountId = extras[ACCOUNT_ID]!!.toInt()
		if (extras.containsKey(HoverAction.NOTE_KEY)) note = extras[HoverAction.NOTE_KEY]
	}

	private fun generateDescription(action: HoverAction, contact: StaxContact?, c: Context): String {
		if (isRecorded) return c.getString(R.string.descrip_recorded, action.from_institution_name)
		val amountStr = Utils.formatAmount(amount)
		return when(transaction_type) {
			HoverAction.RECEIVE -> c.getString(R.string.descrip_transfer_received, contact!!.shortName())
			HoverAction.FETCH_ACCOUNTS -> c.getString(R.string.descrip_fetch_accounts, action.from_institution_name)
			HoverAction.BALANCE -> c.getString(R.string.descrip_balance, action.from_institution_name)
			HoverAction.AIRTIME -> c.getString(R.string.descrip_airtime_sent, amountStr,
				if (contact == null) c.getString(R.string.self_choice) else contact.shortName())
			HoverAction.P2P -> c.getString(R.string.descrip_transfer_sent, amountStr, contact!!.shortName())
			HoverAction.ME2ME -> c.getString(R.string.descrip_transfer_sent, amountStr, action.to_institution_name)
			HoverAction.C2B -> c.getString(R.string.descrip_bill_paid, amountStr, action.to_institution_name)
			else -> "Other"
		}
	}

	val isRetryable: Boolean
		get() = transaction_type == HoverAction.P2P || transaction_type == HoverAction.AIRTIME
				|| transaction_type == HoverAction.BALANCE

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

	override fun toString(): String {
		return description
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