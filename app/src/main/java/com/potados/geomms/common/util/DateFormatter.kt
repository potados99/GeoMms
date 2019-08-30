package com.potados.geomms.common.util

import android.content.Context
import android.text.format.DateFormat
import com.potados.geomms.R
import com.potados.geomms.common.extension.isSameDay
import com.potados.geomms.common.extension.isSameWeek
import com.potados.geomms.common.extension.isSameYear
import java.text.SimpleDateFormat
import java.util.*


class DateFormatter(val context: Context) {

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