package com.hover.stax.schedules;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.database.Constants;
import com.hover.stax.utils.DateUtils;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

@Entity(tableName = "schedules")
public class Schedule {
	public final static String ONCE = "4once", DAILY = "0daily", WEEKLY = "1weekly", BIWEEKLY = "2biweekly", MONTHLY = "3monthly",
			SCHEDULE_ID = "schedule_id", DATE_KEY = "date";

	@PrimaryKey(autoGenerate = true)
	@NonNull
	public int id;

	@NonNull
	@ColumnInfo(name = "type")
	public String type;

	@ColumnInfo(name = "channel_id")
	public int channel_id;

	@ColumnInfo(name = "action_id")
	public String action_id;

	@NonNull
	@ColumnInfo(name = "recipient")
	public String recipient;

	@ColumnInfo(name = "amount")
	public String amount;

	@ColumnInfo(name = "note")
	public String note;

	@NonNull
	@ColumnInfo(name = "description")
	public String description;

	@NonNull
	@ColumnInfo(name = "start_date", defaultValue = "CURRENT_TIMESTAMP")
	public Long start_date;

	@ColumnInfo(name = "end_date", defaultValue = "CURRENT_TIMESTAMP")
	public Long end_date;

	@NonNull
	@ColumnInfo(name = "frequency")
	public String frequency;

	@NonNull
	@ColumnInfo(name = "complete", defaultValue = "false")
	public boolean complete;

	public Schedule() {}

	public Schedule(Action action, Long date, String r, String a, String n, Context c) {
		this(date, r, a, n);
		type = action.transaction_type;
		channel_id = action.channel_id;
		action_id = action.public_id;
		description = generateDescription(action, c);
	}

	public Schedule(Long date, String r, String a, String n, Context c) {
		this(date, r, a, n);
		type = Constants.REQUEST_TYPE;
		description = generateDescription(null, c);
	}

	public Schedule(Long date, String r, String a, String n) {
		start_date = date;
		end_date = date;
		recipient = r;
		amount = a;
		note = n;
		frequency = ONCE;
		complete = false;
	}

	private String generateDescription(Action action, Context c) {
		switch (type) {
			case Action.AIRTIME:
				return c.getString(R.string.schedule_descrip_airtime, action.from_institution_name, ((recipient == null || recipient.equals("")) ? "myself" : recipient));
			case Action.P2P:
				return c.getString(R.string.transaction_descrip_money, action.from_institution_name, recipient);
			case Action.ME2ME:
				return c.getString(R.string.transaction_descrip_money, action.from_institution_name, action.to_institution_name);
			case Constants.REQUEST_TYPE:
				return c.getString(R.string.request_descrip, recipient);
			default:
				return "Other";
		}
	}

	public String humanFrequency(Context c) {
		switch (frequency) {
			case Schedule.DAILY:
				return c.getString(R.string.daily);
			case Schedule.WEEKLY:
				return c.getString(R.string.weekly);
			case Schedule.BIWEEKLY:
				return c.getString(R.string.biweekly);
			case Schedule.MONTHLY:
				return c.getString(R.string.monthly);
			default:
				return DateUtils.humanFriendlyDate(start_date);
		}
	}

	String title(Context c) {
		switch (type) {
			case Action.P2P:
			case Action.ME2ME:
				return c.getString(R.string.notify_transfer_cta);
			case Action.AIRTIME:
				return c.getString(R.string.notify_airtime_cta);
			case Constants.REQUEST_TYPE:
				return c.getString(R.string.notify_request_cta);
			default:
				return null;
		}
	}

	String notificationMsg(Context c) {
		switch (type) {
			case Action.P2P:
			case Action.ME2ME:
				return c.getString(R.string.notify_transfer, description);
			case Action.AIRTIME:
				return c.getString(R.string.notify_airtime);
			case Constants.REQUEST_TYPE:
				return c.getString(R.string.notify_request, recipient);
			default:
				return null;
		}
	}

	boolean isScheduledForToday() {
		Date scheduledDate = new Date(start_date);
		Date today = new Date(DateUtils.now());
		SimpleDateFormat comparisonFormat = new SimpleDateFormat("MM dd yyyy");
		return comparisonFormat.format(scheduledDate).equals(comparisonFormat.format(today));
	}

	@NotNull
	@Override
	public String toString() {
		return description;
	}
}
