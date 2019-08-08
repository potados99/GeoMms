package com.potados.geomms.util

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class DateTime(val timeStamp: Long) : Serializable {

    private val date = Date(timeStamp)

    fun toString(format: String): String {
        return SimpleDateFormat(format, Locale.getDefault()).format(date)
    }

    override fun toString(): String {
        return toString("yyyy-MM-dd hh:mm:ss")
    }

    fun toShortenString(): String {
        val nowDate = Date()
        val now = Calendar.getInstance().apply {
            time = nowDate
        }

        val thenDate = date
        val then = Calendar.getInstance().apply {
            time = thenDate
        }

        val deltaDate = now.get(Calendar.DATE) - then.get(Calendar.DATE)

        if (deltaDate > 6) {
            /**
             * 일주일 지남
             */

            return SimpleDateFormat("MM/dd/yy", Locale.KOREA).format(thenDate)
        }
        else if (deltaDate > 1) {
            /**
             * 하루 초과 지남
             */

            return SimpleDateFormat("E", Locale.KOREA).format(thenDate)
        }
        else if (deltaDate > 0) {
            /**
             * 하루 지남
             */

            return "Yesterday"
        }
        else {
            /**
             * 아직 당일임
             */

            return SimpleDateFormat("HH:mm", Locale.KOREA).format(thenDate)
        }
    }

    fun durationUntil(then: DateTime): Duration {
        return Duration.between(then, this)
    }

    fun durationUntilNow(): Duration {
        return Duration.between(now(), this)
    }

    companion object {
        fun getCurrentTimeStamp(): Long {
            return System.currentTimeMillis()
        }

        fun now(): DateTime {
            return DateTime(getCurrentTimeStamp())
        }
    }
}