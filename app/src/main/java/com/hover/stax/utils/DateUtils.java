package com.hover.stax.utils;

import android.content.Context;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.hover.stax.R;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtils {
	public final static int DAY = 24 * 60 * 60 * 1000;

	public static long now() {
		return new Date().getTime();
	}

	public static long today() {
		return MaterialDatePicker.todayInUtcMilliseconds();
	}

	public static String timeUntil(Context context, final long millis) {
		return humanFriendlyTime(context, millis - System.currentTimeMillis());
	}

	public static String timeAgo(Context context, final long millis) {
		return humanFriendlyTime(context, System.currentTimeMillis() - millis);
	}

	public static String humanFriendlyDate(long timestamp) {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(now());
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(timestamp);
		String str = monthNumToName(date.get(Calendar.MONTH));
		str += " " + date.get(Calendar.DAY_OF_MONTH);
		if (date.get(Calendar.YEAR) != now.get(Calendar.YEAR))
			str += " " + date.get(Calendar.YEAR);
		return str;
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

	// All calculations need 1 added since the transaction happens on the start and end. Including it in the partial calculations breaks it tho
	public static int getDays(Long start_date, Long end_date) {
		return calculateDays(start_date, end_date) + 1;
	}
	private static int calculateDays(Long start_date, Long end_date) {
		long diff = end_date - start_date;
		return (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	}

	// Simply taking the floor for weeks and biweeks should work since any remainder of a week doesn't matter
	public static int getWeeks(Long start_date, Long end_date) {
		return calculateWeeks(start_date, end_date) + 1;
	}
	private static int calculateWeeks(Long start_date, Long end_date) {
		return calculateDays(start_date, end_date) / 7;
	}

	public static int getBiweeks(Long start_date, Long end_date) {
		return calculateBiweeks(start_date, end_date) + 1;
	}
	private static int calculateBiweeks(Long start_date, Long end_date) {
		return calculateWeeks(start_date, end_date) / 2;
	}

	public static int getMonths(Long start_date, Long end_date) {
		return calculateMonths(start_date, end_date) + 1;
	}
	private static int calculateMonths(Long start_date, Long end_date) {
		Calendar start = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		start.setTimeInMillis(start_date);
		end.setTimeInMillis(end_date);
		if (end.get(Calendar.DAY_OF_MONTH) < start.get(Calendar.DAY_OF_MONTH))
			end.add(Calendar.MONTH, -1);
		int years = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);
		int months = end.get(Calendar.MONTH) - start.get(Calendar.MONTH);
		return (years * 12) + months;
	}

	public static Long getDate(Long start_date, int frequency, int times) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(start_date);
		switch (frequency) {
			case 0: cal.add(Calendar.DATE, times); break;
			case 1: cal.add(Calendar.WEEK_OF_YEAR, times); break;
			case 2: cal.add(Calendar.WEEK_OF_YEAR, times*2); break;
			case 3: cal.add(Calendar.MONTH, times); break;
		}
		return cal.getTimeInMillis();
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