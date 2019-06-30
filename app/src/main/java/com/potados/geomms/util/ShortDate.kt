package com.potados.geomms.util

import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

class ShortDate {
    companion object {
        fun of(timestamp: Long): String {
            val nowDate = Date()
            val now = Calendar.getInstance().apply {
                time = nowDate
            }

            val thenDate = Date(timestamp)
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

    }
}
