package com.hover.stax.requests;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hover.stax.R;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.utils.DateUtils;

@Entity(tableName = "requests")
public class Request {

	@PrimaryKey(autoGenerate = true)
	@NonNull
	public int id;

	@ColumnInfo(name = "description")
	public String description;

	@NonNull
	@ColumnInfo(name = "recipient_ids")
	public String recipient_ids;

	@ColumnInfo(name = "amount")
	public String amount;

	@ColumnInfo(name = "note")
	public String note;

	@ColumnInfo(name = "message")
	public String message;

	@ColumnInfo(name = "matched_transaction_uuid")
	public String matched_transaction_uuid;

	@NonNull
	@ColumnInfo(name = "date_sent", defaultValue = "CURRENT_TIMESTAMP")
	public Long date_sent;

	public Request() { }

	public Request(StaxContact contact, String a, String n, Context context) {
		recipient_ids = contact.id;
		amount = a;
		note = n;
		date_sent = DateUtils.now();
		description = getDescription(contact, context);
	}

	public String getDescription(StaxContact contact, Context c) {
		return c.getString(R.string.descrip_request, contact.shortName());
	}
}
