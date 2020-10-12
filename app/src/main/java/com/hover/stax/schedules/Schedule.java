package com.hover.stax.schedules;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hover.stax.R;
import com.hover.stax.actions.Action;
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
	@ColumnInfo(name = "type") // request, airtime, p2p, me2me
	public String type;

	@NonNull
	@ColumnInfo(name = "channel_id")
	public int channel_id;

	@NonNull
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

	public Schedule() {
	}

	public Schedule(Action action, Long date, String r, String a, Context c) {
		type = action.transaction_type;
		channel_id = action.channel_id;
		action_id = action.public_id;
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
				return c.getString(R.string.schedule_descrip_airtime, action.from_institution_name, ((recipient == null || recipient.equals("")) ? "myself" : recipient));
			case Action.P2P:
				return c.getString(R.string.transaction_descrip_money, action.from_institution_name, recipient);
			case Action.ME2ME:
				return c.getString(R.string.transaction_descrip_money, action.from_institution_name, action.to_institution_name);
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
