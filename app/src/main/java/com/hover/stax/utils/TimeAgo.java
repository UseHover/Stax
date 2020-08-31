package com.hover.stax.utils;

import android.content.Context;

import com.hover.stax.R;

import java.util.Date;

public class TimeAgo {

    private TimeAgo() {
        // Nothing
    }

    public static String timeUntil(Context context, final Date date) {
        return timeUntil(context, date.getTime());
    }

    public static String timeAgo(Context context, final Date date) {
        return timeAgo(context, date.getTime());
    }

    public static String timeUntil(Context context, final long millis) {
        return time(context, millis - System.currentTimeMillis());
    }

    public static String timeAgo(Context context, final long millis) {
        return time(context, System.currentTimeMillis() - millis);
    }

    private static String time(Context context, long diffMillis) {
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

}