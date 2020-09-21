package com.hover.stax.transactions;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.HashMap;

@Entity(tableName = "stax_transactions")
public class StaxTransaction {

	@PrimaryKey(autoGenerate = true)
	@NonNull
	public int id;

	@NonNull
	@ColumnInfo(name = "uuid")
	public String uuid;

	@NonNull
	@ColumnInfo(name = "action_id")
	public String action_id;

	@NonNull
	@ColumnInfo(name = "transaction_type")
	public String transaction_type;

	@NonNull
	@ColumnInfo(name = "channel_id")
	public int channel_id;

	@NonNull
	@ColumnInfo(name = "status")
	public String status;

	@NonNull
	@ColumnInfo(name = "initiated_at", defaultValue = "CURRENT_TIMESTAMP")
	public Long initiated_at;

	@NonNull
	@ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
	public Long updated_at;

	@ColumnInfo(name = "description")
	public String description;

	@ColumnInfo(name = "amount")
	public String amount;

	@ColumnInfo(name = "fee")
	public String fee;

	@ColumnInfo(name = "recipient")
	public String recipient;

	@ColumnInfo(name = "fromInstitutionStartPos")
	public int fromInstitutionStartPos;

	@ColumnInfo(name = "fromInstitutionEndPos")
	public int fromInstitutionEndPos;

	@ColumnInfo(name = "month")
	public int month;

	@ColumnInfo(name = "year")
	public int year;

	public StaxTransaction() {}

	public StaxTransaction(Intent data, Action action, Context c) {
		if (data.hasExtra(TransactionContract.COLUMN_UUID) && data.getStringExtra(TransactionContract.COLUMN_UUID) != null) {
			uuid = data.getStringExtra(TransactionContract.COLUMN_UUID);
			action_id = data.getStringExtra(TransactionContract.COLUMN_ACTION_ID);
			transaction_type = data.getStringExtra(TransactionContract.COLUMN_TYPE);
			channel_id = data.getIntExtra(TransactionContract.COLUMN_CHANNEL_ID, -1);
			status = data.getStringExtra(TransactionContract.COLUMN_STATUS);
			initiated_at = data.getLongExtra(TransactionContract.COLUMN_REQUEST_TIMESTAMP, DateUtils.now());
			updated_at = initiated_at;
			setMonthAndYear(updated_at);

			HashMap<String, String> extras = (HashMap<String, String>) data.getSerializableExtra(TransactionContract.COLUMN_INPUT_EXTRAS);
			if (extras != null) {
				if (extras.containsKey(Action.AMOUNT_KEY))
					amount = Utils.formatAmountV2((extras.get(Action.AMOUNT_KEY)));
				if (extras.containsKey(Action.PHONE_KEY))
					recipient = extras.get(Action.PHONE_KEY);
				else if (extras.containsKey(Action.ACCOUNT_KEY))
					recipient = extras.get(Action.ACCOUNT_KEY);
			}
			if (transaction_type != null)
				description = generateDescription(action, c);
		}
	}

	public void update(Intent data) {
		status = data.getStringExtra(TransactionContract.COLUMN_STATUS);
		updated_at = data.getLongExtra(TransactionContract.COLUMN_UPDATE_TIMESTAMP, DateUtils.now());
		setMonthAndYear(updated_at);

		HashMap<String, String> extras = (HashMap<String, String>) data.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES);
		if (extras.containsKey(Action.FEE_KEY))
			fee = Utils.formatAmountV2(extras.get(Action.FEE_KEY));
	}
	private void setMonthAndYear(long timestamp) {
		StaxDate staxDate = DateUtils.getStaxDate(timestamp);
		month = staxDate.getMonth();
		year = staxDate.getYear();
	}

	private String generateDescription(Action action, Context c) {
		switch (transaction_type) {
			case Action.AIRTIME:
				String sender = action.from_institution_name+" ";
				Log.e("STAX", (action.from_institution_name.equals("null") + ""));
				setStartAndEndPosForInstitution(0, sender);
				return c.getString(R.string.transaction_descrip_airtime, sender, ((recipient == null || recipient.equals("")) ? "myself" : recipient));
			case Action.P2P:
				setStartAndEndPosForInstitution(1, action.from_institution_name);
				return c.getString(R.string.transaction_descrip_money, action.to_institution_name, recipient);
			case Action.ME2ME:
				setStartAndEndPosForInstitution(1, action.from_institution_name);
				return c.getString(R.string.transaction_descrip_money, action.from_institution_name, action.to_institution_name);
			default:
				return "Other";
		}
	}

	private void setStartAndEndPosForInstitution(int startPos, String institutionName) {
		fromInstitutionStartPos = startPos;
		if(institutionName.split(" ").length > 1) fromInstitutionEndPos = startPos + institutionName.split(" ").length;
		else fromInstitutionEndPos = startPos;
	}




	@NotNull
	@Override
	public String toString() {
		return description;
	}
}
