package com.hover.stax.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hover.stax.R
import kotlin.math.abs

@Composable
fun TimeStringGenerator(timestamp: Long): String {
    val millisSince = System.currentTimeMillis() - timestamp

    val seconds = (abs(millisSince) / 1000).toDouble()
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val years = days / 365
    val time: String = when {
        seconds < 45 -> {
            stringResource(id = R.string.timeago_secondes)
        }
        seconds < 90 -> {
            stringResource(R.string.timeago_minute)
        }
        minutes < 45 -> {
            stringResource(R.string.timeago_minutes, Math.round(minutes))
        }
        minutes < 90 -> {
            stringResource(R.string.timeago_hour)
        }
        hours < 24 -> {
            stringResource(R.string.timeago_hours, Math.round(hours))
        }
        hours < 42 -> {
            stringResource(R.string.timeago_day)
        }
        days < 30 -> {
            stringResource(R.string.timeago_days, Math.round(Math.floor(days)))
        }
        days < 45 -> {
            stringResource(R.string.timeago_month)
        }
        days < 365 -> {
            stringResource(R.string.timeago_months, Math.round(Math.floor(days / 30)))
        }
        years < 1.5 -> {
            stringResource(R.string.timeago_year)
        }
        else -> {
            stringResource(R.string.timeago_years, Math.round(Math.floor(years)))
        }
    }
    return if (millisSince < 0) {
        stringResource(R.string.timeago_from_now, time)
    } else {
        stringResource(R.string.timeago_ago, time)
    }
}