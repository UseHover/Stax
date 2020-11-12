package com.hover.stax.requests;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hover.stax.R;
import com.hover.stax.utils.DateUtils;

@Entity(tableName = "requests")
public class Request {

	@PrimaryKey(autoGenerate = true)
	@NonNull
	public int id;

	@NonNull
	@ColumnInfo(name = "recipient")
	public String recipient;

	@ColumnInfo(name = "amount")
	public String amount;

	@ColumnInfo(name = "note")
	public String note;

	@ColumnInfo(name = "message")
	public String message;

	@ColumnInfo(name = "matched_transaction_uuid")
	public String matched_transaction_uuid;

	@ColumnInfo(name = "receiving_channel_id")
	public int receiving_channel_id;

	@ColumnInfo(name  = "receiving_account_number")
	public String receiving_account_number;

	@NonNull
	@ColumnInfo(name = "date_sent", defaultValue = "CURRENT_TIMESTAMP")
	public Long date_sent;

	public Request() {
	}

	public Request(@NonNull String recipient, String amount, String note, int receiving_channel_id, String receiving_account_number) {
		this.recipient = recipient;
		this.amount = amount;
		this.note = note;
		this.receiving_channel_id = receiving_channel_id;
		this.receiving_account_number = receiving_account_number;
		date_sent = DateUtils.now();
	}

	public String getDescription(Context c) {
		return c.getString(R.string.descrip_request, recipient);
	}
}
