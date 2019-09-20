/**
 * Copyright (C) 2019 Song Byeong Jun and original authors
 *
 * This file is part of GeoMms.
 *
 * This software makes use of third-party patent which belongs to
 * KANG MOON KYOU and LEE GWI BONG:
 * System and Method for sharing service of location information
 * 10-1235884-0000 (2013.02.15)
 *
 * GeoMms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoMms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoMms.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.potados.geomms.common.util

import android.content.Context
import android.text.format.DateFormat
import com.potados.geomms.R
import com.potados.geomms.base.FailableComponent
import com.potados.geomms.common.extension.isSameDay
import com.potados.geomms.common.extension.isSameWeek
import com.potados.geomms.common.extension.isSameYear
import java.text.SimpleDateFormat
import java.util.*


class DateFormatter(val context: Context) : FailableComponent() {

    /**
     * Formats the [pattern] correctly for the current locale, and replaces 12 hour format with
     * 24 hour format if necessary
     */
    private fun getFormatter(pattern: String): SimpleDateFormat {
        var formattedPattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), pattern)

        if (DateFormat.is24HourFormat(context)) {
            formattedPattern = formattedPattern.replace("h", "HH").replace(" a".toRegex(), "")
        }

        return SimpleDateFormat(formattedPattern, Locale.getDefault())
    }

    fun getDetailedTimestamp(date: Long): String {
        return getFormatter("M/d/y, h:mm:ss a").format(date)
    }

    fun getTimestamp(date: Long): String {
        return getFormatter("h:mm a").format(date)
    }

    fun getMessageTimestamp(date: Long): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance()
        then.timeInMillis = date

        return when {
            now.isSameDay(then) -> getFormatter("h:mm a")
            now.isSameWeek(then) -> getFormatter("E h:mm a")
            now.isSameYear(then) -> getFormatter("MMM d, h:mm a")
            else -> getFormatter("MMM d yyyy, h:mm a")
        }.format(date)
    }

    fun getConversationTimestamp(date: Long): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance()
        then.timeInMillis = date

        return when {
            now.isSameDay(then) -> getFormatter("h:mm a")
            now.isSameWeek(then) -> getFormatter("E")
            now.isSameYear(then) -> getFormatter("MMM d")
            else -> getFormatter("MM/d/yy")
        }.format(date)
    }

    fun getScheduledTimestamp(date: Long): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance()
        then.timeInMillis = date

        return when {
            now.isSameDay(then) -> getFormatter("h:mm a")
            now.isSameYear(then) -> getFormatter("MMM d h:mm a")
            else -> getFormatter("MMM d yyyy h:mm a")
        }.format(date)
    }

    fun getDuration(duration: Long, short: Boolean = false): String {
        val toSec = (duration / 1000).toInt()

        val hours = toSec / 3600
        val minutes = toSec % 3600 / 60
        val seconds = toSec % 60

        val builder = StringBuilder()

        if (hours > 0) {
            // On for the string, one for the format.
            builder.append(context.resources.getQuantityString(if (short) R.plurals.hour_short else R.plurals.hour, hours, hours))
        }

        if (minutes > 0) {
            builder.append(' ')
            builder.append(context.resources.getQuantityString(if (short) R.plurals.minute_short else R.plurals.minute, minutes, minutes))
        }

        if (toSec < 60) {
            builder.append(' ')
            builder.append(context.resources.getQuantityString(if (short) R.plurals.second_short else R.plurals.second, seconds, seconds))
        }

        return builder.toString()
    }

}