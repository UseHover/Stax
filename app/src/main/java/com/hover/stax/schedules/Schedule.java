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
import java.util.Calendar;
import java.util.Date;

@Entity(tableName = "schedules")
public class Schedule {
	public final static int DAILY = 0, WEEKLY = 1, BIWEEKLY = 2, MONTHLY = 3, ONCE = 4;
	public final static String SCHEDULE_ID = "schedule_id", DATE_KEY = "date";

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
	public int frequency;

	@NonNull
	@ColumnInfo(name = "complete", defaultValue = "false")
	public boolean complete;

	public Schedule() {}

	public Schedule(Action action, Long start, Boolean isRepeat, int frequency, Long end, String r, String a, String n, Context c) {
		this(start, r, a, n);
		setRepeatVals(isRepeat, frequency, end);
		type = action.transaction_type;
		channel_id = action.channel_id;
		action_id = action.public_id;
		description = generateDescription(action, c);
	}

	public Schedule(Long start, Boolean isRepeat, int frequency, Long end, String r, String a, String n, Context c) {
		this(start, r, a, n);
		setRepeatVals(isRepeat, frequency, end);
		type = Constants.REQUEST_TYPE;
		description = generateDescription(null, c);
	}

	public Schedule(Long date, String r, String a, String n) {
		start_date = date == null ? DateUtils.today() : date;
		recipient = r;
		amount = a;
		note = n;
		complete = false;
	}

	private void setRepeatVals(Boolean isRepeat, int freq, Long end) {
		frequency = isRepeat ? freq : ONCE;
		end_date = isRepeat ? end : start_date;
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
		if (frequency == ONCE)
			return DateUtils.humanFriendlyDate(start_date);
		else
			return c.getResources().getStringArray(R.array.frequency_array)[frequency];
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
		switch (frequency) {
			case DAILY: return dateInRange();
			case WEEKLY: return onDayOfWeek();
			case BIWEEKLY: return onDayOfBiweek();
			case MONTHLY: return onDayOfMonth();
			default: return checkDateMatch(new Date(start_date));
		}
	}

	private boolean checkDateMatch(Date d) {
		Date today = new Date(DateUtils.now());
		SimpleDateFormat comparisonFormat = new SimpleDateFormat("MM dd yyyy");
		return comparisonFormat.format(d).equals(comparisonFormat.format(today));
	}

	private boolean dateInRange() {
		Date today = new Date(DateUtils.today());
		return !today.before(new Date(start_date)) && (end_date == null || !today.after(new Date(end_date)));
	}

	private boolean onDayOfWeek() {
		Calendar today = Calendar.getInstance();
		today.setTimeInMillis(DateUtils.today());
		Calendar start = Calendar.getInstance();
		start.setTimeInMillis(start_date);
		return dateInRange() && today.get(Calendar.DAY_OF_WEEK) == start.get(Calendar.DAY_OF_WEEK);
	}

	private boolean onDayOfBiweek() {
		return onDayOfWeek() && isEvenWeeksSince();
	}

	private boolean isEvenWeeksSince() {
		Calendar today = Calendar.getInstance();
		today.setTimeInMillis(DateUtils.today());
		Calendar start = Calendar.getInstance();
		start.setTimeInMillis(start_date);
		return (Math.abs(start.get(Calendar.WEEK_OF_YEAR) - today.get(Calendar.WEEK_OF_YEAR)) % 2) == 0;
	}

	private boolean onDayOfMonth() {
		Calendar today = Calendar.getInstance();
		today.setTimeInMillis(DateUtils.today());
		Calendar start = Calendar.getInstance();
		start.setTimeInMillis(start_date);
		return dateInRange() && (start.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) ||
            (start.get(Calendar.DAY_OF_MONTH) > 28 && start.get(Calendar.DAY_OF_MONTH) > today.getActualMaximum(Calendar.DAY_OF_MONTH) && today.get(Calendar.DAY_OF_MONTH) == today.getActualMaximum(Calendar.DAY_OF_MONTH)));
	}

	@NotNull
	@Override
	public String toString() {
		return description;
	}
}
