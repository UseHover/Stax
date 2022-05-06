package com.hover.stax.transactions

import android.content.Context
import com.hover.stax.utils.DateUtils.now
import com.hover.stax.utils.Utils.getAmount
import com.hover.stax.utils.Utils.formatAmount
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import android.content.Intent
import androidx.room.Entity
import androidx.room.Index
import com.hover.sdk.actions.HoverAction
import com.hover.stax.contacts.StaxContact
import com.hover.sdk.transactions.TransactionContract
import timber.log.Timber
import com.hover.stax.transactions.StaxTransaction
import com.hover.stax.R
import com.hover.sdk.api.HoverParameters
import com.hover.sdk.transactions.Transaction
import java.util.HashMap

@Entity(tableName = "stax_transactions", indices = [Index(value = ["uuid"], unique = true)])
class StaxTransaction {
	@PrimaryKey(autoGenerate = true)
	var id = 0

	@ColumnInfo(name = "uuid")
	var uuid: String = null

	@ColumnInfo(name = "action_id")
	var action_id: String = null

	@ColumnInfo(name = "environment", defaultValue = "0")
	var environment: Int = null

	@ColumnInfo(name = "transaction_type")
	var transaction_type: String = null

	@ColumnInfo(name = "channel_id")
	var channel_id = 0

	@ColumnInfo(name = "status", defaultValue = Transaction.PENDING)
	var status: String = null

	@ColumnInfo(name = "category")
	var category: String? = null

	@ColumnInfo(name = "initiated_at", defaultValue = "CURRENT_TIMESTAMP")
	var initiated_at: Long = null

	@ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
	var updated_at: Long = null

	@ColumnInfo(name = "description")
	var description: String? = null

	@ColumnInfo(name = "amount")
	var amount: Double? = null

	@ColumnInfo(name = "fee")
	var fee: Double? = null

	@ColumnInfo(name = "confirm_code")
	var confirm_code: String? = null

	@ColumnInfo(name = "recipient_id")
	var counterparty_id: String? = null

	@ColumnInfo(name = "balance")
	var balance: String? = null

	@ColumnInfo(name = "account_id")
	var accountId: Int? = null

	@ColumnInfo(name = "note")
	var note: String? = null

	// FIXME: DO not use! Below is covered by contact and account models. No easy way to drop column yet, but room 2.4 adds an easy way. Currently alpha, use once it is stable
	@ColumnInfo(name = "counterparty")
	var counterparty: String? = null

	@ColumnInfo(name = "account_name")
	var accountName: String? = null

	constructor() {}
	constructor(data: Intent, action: HoverAction, contact: StaxContact, c: Context) {
		if (data.hasExtra(TransactionContract.COLUMN_UUID) && data.getStringExtra(
				TransactionContract.COLUMN_UUID
			) != null
		) {
			uuid = data.getStringExtra(TransactionContract.COLUMN_UUID)!!
			channel_id = data.getIntExtra(TransactionContract.COLUMN_CHANNEL_ID, -1)
			action_id = data.getStringExtra(TransactionContract.COLUMN_ACTION_ID)!!
			transaction_type = data.getStringExtra(TransactionContract.COLUMN_TYPE)!!
			environment = data.getIntExtra(TransactionContract.COLUMN_ENVIRONMENT, 0)
			status = data.getStringExtra(TransactionContract.COLUMN_STATUS)!!
			category = data.getStringExtra(TransactionContract.COLUMN_CATEGORY)
			initiated_at = data.getLongExtra(TransactionContract.COLUMN_REQUEST_TIMESTAMP, now())
			updated_at =
				data.getLongExtra(TransactionContract.COLUMN_UPDATE_TIMESTAMP, initiated_at)
			counterparty_id = contact.id
			description = generateDescription(action, contact, c)
			parseExtras(data.getSerializableExtra(TransactionContract.COLUMN_INPUT_EXTRAS) as HashMap<String, String>?)
			parseExtras(data.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as HashMap<String, String>?)
			Timber.v("creating transaction with uuid: %s", uuid)
		}
	}

	fun update(data: Intent, action: HoverAction, contact: StaxContact, c: Context) {
		status = data.getStringExtra(TransactionContract.COLUMN_STATUS)!!
		category = data.getStringExtra(TransactionContract.COLUMN_CATEGORY)
		parseExtras(data.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as HashMap<String, String>?)
		if (counterparty_id == null) counterparty_id = contact.id
		description = generateDescription(action, contact, c)
		updated_at = data.getLongExtra(TransactionContract.COLUMN_UPDATE_TIMESTAMP, initiated_at)
	}

	private fun parseExtras(extras: HashMap<String, String>?) {
		if (extras == null) return
		Timber.e("Extras %s", extras.keys)
		if (extras.containsKey(HoverAction.AMOUNT_KEY)) amount = getAmount(
			extras[HoverAction.AMOUNT_KEY]!!
		)
		if (extras.containsKey(FEE_KEY)) fee = getAmount(
			extras[FEE_KEY]!!
		)
		if (extras.containsKey(CONFIRM_CODE_KEY)) confirm_code = extras[CONFIRM_CODE_KEY]
		if (extras.containsKey(HoverAction.BALANCE)) balance = extras[HoverAction.BALANCE]
		if (extras.containsKey(ACCOUNT_ID)) accountId = extras[ACCOUNT_ID]!!.toInt()
		if (extras.containsKey(HoverAction.NOTE_KEY)) note = extras[HoverAction.NOTE_KEY]
	}

	private fun generateDescription(
		action: HoverAction,
		contact: StaxContact?,
		c: Context
	): String {
		return if (isRecorded) c.getString(
			R.string.descrip_recorded,
			action.from_institution_name
		) else when (transaction_type) {
			HoverAction.BALANCE -> c.getString(
				R.string.descrip_balance,
				action.from_institution_name
			)
			HoverAction.AIRTIME -> c.getString(
				R.string.descrip_airtime_sent,
				amount,
				if (contact == null) c.getString(R.string.self_choice) else contact.shortName()
			)
			HoverAction.P2P -> c.getString(
				R.string.descrip_transfer_sent,
				amount,
				contact!!.shortName()
			)
			HoverAction.ME2ME -> c.getString(
				R.string.descrip_transfer_sent,
				amount,
				action.to_institution_name
			)
			HoverAction.C2B -> c.getString(
				R.string.descrip_bill_paid,
				amount,
				action.to_institution_name
			)
			HoverAction.RECEIVE -> c.getString(
				R.string.descrip_transfer_received,
				contact!!.shortName()
			)
			HoverAction.FETCH_ACCOUNTS -> c.getString(
				R.string.descrip_fetch_accounts,
				action.from_institution_name
			)
			else -> "Other"
		}
	}

	//    public TransactionStatus getFullStatus() {
	//        return new TransactionStatus(this);
	//    }
	val isFailed: Boolean
		get() = status == Transaction.FAILED
	val isSuccessful: Boolean
		get() = status == Transaction.SUCCEEDED
	val isBalanceType: Boolean
		get() = transaction_type == HoverAction.BALANCE
	val isRecorded: Boolean
		get() = environment == HoverParameters.MANUAL_ENV

	fun getSignedAmount(a: Double?): String? {
		var str: String? = null
		if (a != null) {
			str = formatAmount(a.toString())
			if (transaction_type != HoverAction.RECEIVE) str = "-$str"
		}
		return str
	}

	val displayBalance: String
		get() = formatAmount(balance)

	override fun toString(): String {
		return description!!
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
	}
}