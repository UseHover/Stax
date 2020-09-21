package com.hover.stax.utils;

import android.content.Context;

import com.hover.stax.R;
import com.hover.stax.transactions.StaxDate;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	public static long now() {
		return new Date().getTime();
	}

	public static String timeUntil(Context context, final long millis) {
		return humanFriendlyTime(context, millis - System.currentTimeMillis());
	}

	public static String timeAgo(Context context, final long millis) {
		return humanFriendlyTime(context, System.currentTimeMillis() - millis);
	}

	public static String humanFriendlyDate(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		String date = monthNumToName(cal.get(Calendar.MONTH));
		date += " " + cal.get(Calendar.DAY_OF_MONTH);
		date += " " + cal.get(Calendar.YEAR);
		return date;
	}

	public static StaxDate getStaxDate(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		return new StaxDate(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR));
	}

	public static StaxDate getPreviousMonthDate(int currentMonth, int currentYear) {
		if (currentMonth == 0) return  new StaxDate(0, currentYear -1);
		else return new StaxDate(currentMonth -1, currentYear);
	}

	public static String humanFriendlyTime(Context context, long diffMillis) {
		double seconds = Math.abs(diffMillis) / 1000;
		double minutes = seconds / 60;
		double hours = minutes / 60;
		double days = hours / 24;
		double years = days / 365;

		final String time;
		if (seconds < 45) {
			time = context.getString(R.string.timeago_secondes);
		} else if (seconds < 90) {
			time = context.getString(R.string.timeago_minute);
		} else if (minutes < 45) {
			time = context.getString(R.string.timeago_minutes, Math.round(minutes));
		} else if (minutes < 90) {
			time = context.getString(R.string.timeago_hour);
		} else if (hours < 24) {
			time = context.getString(R.string.timeago_hours, Math.round(hours));
		} else if (hours < 42) {
			time = context.getString(R.string.timeago_day);
		} else if (days < 30) {
			time = context.getString(R.string.timeago_days, Math.round(Math.floor(days)));
		} else if (days < 45) {
			time = context.getString(R.string.timeago_month);
		} else if (days < 365) {
			time = context.getString(R.string.timeago_months, Math.round(Math.floor(days / 30)));
		} else if (years < 1.5) {
			time = context.getString(R.string.timeago_year);
		} else {
			time = context.getString(R.string.timeago_years, Math.round(Math.floor(years)));
		}

		if (diffMillis < 0) {
			return context.getString(R.string.timeago_from_now, time);
		} else {
			return context.getString(R.string.timeago_ago, time);
		}
	}



	public static String monthNumToName(int number) {
		switch (number) {
			case 0:
				return "January";
			case 1:
				return "February";
			case 2:
				return "March";
			case 3:
				return "April";
			case 4:
				return "May";
			case 5:
				return "June";
			case 6:
				return "July";
			case 7:
				return "August";
			case 8:
				return "September";
			case 9:
				return "October";
			case 10:
				return "November";
			default:
				return "December";
		}
	}


}