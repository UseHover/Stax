/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.utils

import com.google.android.material.datepicker.MaterialDatePicker
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {
    const val MIN = 60 * 1000
    const val DAY = 24 * 60 * MIN

    @JvmStatic
    fun now(): Long {
        return Date().time
    }

    @JvmStatic
    fun today(): Long {
        return MaterialDatePicker.todayInUtcMilliseconds()
    }

    fun todayDate(): Date = Calendar.getInstance().time

    @JvmStatic
    fun humanFriendlyDate(timestamp: Long): String {
        if (timestamp == -1L) return ""
        val now = Calendar.getInstance()
        now.timeInMillis = now()
        val date = Calendar.getInstance()
        date.timeInMillis = timestamp
        var str = monthNumToName(date[Calendar.MONTH])
        str += " " + date[Calendar.DAY_OF_MONTH]

        if (date[Calendar.YEAR] != now[Calendar.YEAR]) str += " " + date[Calendar.YEAR]
        return str
    }

    @JvmStatic
    fun humanFriendlyDateTime(timestamp: Long): String {
        val time = Calendar.getInstance()
        time.timeInMillis = timestamp
        var str = String.format("%02d:%02d", time[Calendar.HOUR_OF_DAY], time[Calendar.MINUTE])
        str += " on " + humanFriendlyDate(timestamp)
        return str
    }

    // All calculations need 1 added since the transaction happens on the start and end. Including it in the partial calculations breaks it tho
    fun getDays(start_date: Long, end_date: Long): Int {
        return calculateDays(start_date, end_date) + 1
    }

    private fun calculateDays(start_date: Long, end_date: Long): Int {
        val diff = end_date - start_date
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
    }

    // Simply taking the floor for weeks and biweeks should work since any remainder of a week doesn't matter
    fun getWeeks(start_date: Long, end_date: Long): Int {
        return calculateWeeks(start_date, end_date) + 1
    }

    private fun calculateWeeks(start_date: Long, end_date: Long): Int {
        return calculateDays(start_date, end_date) / 7
    }

    fun getBiweeks(start_date: Long, end_date: Long): Int {
        return calculateBiweeks(start_date, end_date) + 1
    }

    private fun calculateBiweeks(start_date: Long, end_date: Long): Int {
        return calculateWeeks(start_date, end_date) / 2
    }

    fun getMonths(start_date: Long, end_date: Long): Int {
        return calculateMonths(start_date, end_date) + 1
    }

    private fun calculateMonths(start_date: Long, end_date: Long): Int {
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()
        start.timeInMillis = start_date
        end.timeInMillis = end_date
        if (end[Calendar.DAY_OF_MONTH] < start[Calendar.DAY_OF_MONTH]) end.add(Calendar.MONTH, -1)
        val years = end[Calendar.YEAR] - start[Calendar.YEAR]
        val months = end[Calendar.MONTH] - start[Calendar.MONTH]
        return years * 12 + months
    }

    fun getDate(start_date: Long?, frequency: Int, times: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = start_date!!
        when (frequency) {
            0 -> cal.add(Calendar.DATE, times)
            1 -> cal.add(Calendar.WEEK_OF_YEAR, times)
            2 -> cal.add(Calendar.WEEK_OF_YEAR, times * 2)
            3 -> cal.add(Calendar.MONTH, times)
        }
        return cal.timeInMillis
    }

    private fun currentYear(): Int = Calendar.getInstance()[Calendar.YEAR]
    private fun currentMonth(): Int = Calendar.getInstance()[Calendar.MONTH]

    fun getDate(expectedWeek: Int) = getDate(null, expectedWeek)
    fun getDate(expectedDay: Int?, expectedWeek: Int): Date {
        val cacheCalendar: Calendar = Calendar.getInstance()
        cacheCalendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, expectedWeek)
        expectedDay?.let { cacheCalendar.set(Calendar.DAY_OF_WEEK, it) }
        cacheCalendar.set(Calendar.MONTH, currentMonth())
        cacheCalendar.set(Calendar.YEAR, currentYear())
        return cacheCalendar.time
    }

    fun getDate(dateLong: Long): Date {
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateLong
        return cal.time
    }

    fun beginningOfTheMonth(): Date {
        val cacheCalendar: Calendar = Calendar.getInstance()
        cacheCalendar.set(Calendar.MONTH, currentMonth())
        cacheCalendar.set(Calendar.YEAR, currentYear())
        cacheCalendar.set(Calendar.DAY_OF_MONTH, 1)
        return cacheCalendar.time
    }

    fun twoMonthsAgo(): Date {
        val c: Calendar = Calendar.getInstance()
        c.add(Calendar.MONTH, -2)
        val month: Int = c[Calendar.MONTH] + 1

        c.set(Calendar.MONTH, month + 1)
        c.set(Calendar.YEAR, c[Calendar.YEAR])
        c.set(Calendar.DAY_OF_MONTH, 1)
        return c.time
    }

    @JvmStatic
    fun lastMonth(): Pair<Int, Int> {
        val c = Calendar.getInstance()
        c.add(Calendar.MONTH, -1)
        val month: Int = c[Calendar.MONTH] + 1
        val year: Int = c[Calendar.YEAR]
        return Pair(month, year)
    }

    private fun monthNumToName(number: Int): String {
        return when (number) {
            0 -> "January"
            1 -> "February"
            2 -> "March"
            3 -> "April"
            4 -> "May"
            5 -> "June"
            6 -> "July"
            7 -> "August"
            8 -> "September"
            9 -> "October"
            10 -> "November"
            else -> "December"
        }
    }
}