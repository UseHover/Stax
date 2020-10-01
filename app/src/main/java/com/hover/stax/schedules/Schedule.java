package com.hover.stax.schedules;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hover.stax.R;
import com.hover.stax.actions.Action;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "schedules")
public class Schedule {
	public final static String ONCE = "4once", DAILY = "0daily", WEEKLY = "1weekly", BIWEEKLY = "2biweekly", MONTHLY = "3monthly";

	@PrimaryKey(autoGenerate = true)
	@NonNull
	public int id;

	@NonNull
	@ColumnInfo(name = "type") // request, airtime, p2p, me2me
	public String type;

	@NonNull
	@ColumnInfo(name = "channel_id")
	public int channel_id;

	@ColumnInfo(name = "action_id")
	public String action_id;

	@NonNull
	@ColumnInfo(name = "recipient")
	public String recipient;

	@ColumnInfo(name = "amount")
	public String amount;

	@ColumnInfo(name = "reason")
	public String reason;

	@NonNull
	@ColumnInfo(name = "description")
	public String description;

	@NonNull
	@ColumnInfo(name = "start_date", defaultValue = "CURRENT_TIMESTAMP")
	public Long start_date;

	@ColumnInfo(name = "end_date", defaultValue = "CURRENT_TIMESTAMP")
	public Long end_date;

	@ColumnInfo(name = "frequency")
	public String frequency;

	public Schedule() {}

	public Schedule(Action action, Long date, String r, String a, Context c) {
		type = action.transaction_type;
		channel_id = action.channel_id;
		start_date = date;
		end_date = date;
		recipient = r;
		amount = a;
		description = generateDescription(action, c);
		frequency = ONCE;
	}

	private String generateDescription(Action action, Context c) {
		switch (type) {
			case Action.AIRTIME:
				return c.getString(R.string.transaction_descrip_airtime, action.from_institution_name, ((recipient == null || recipient.equals("")) ? "myself" : recipient));
			case Action.P2P:
				return c.getString(R.string.transaction_descrip_money, action.to_institution_name, recipient);
			case Action.ME2ME:
				return c.getString(R.string.transaction_descrip_money, action.from_institution_name, action.to_institution_name);
			default:
				return "Other";
		}
	}

	@NotNull
	@Override
	public String toString() {
		return description;
	}
}
