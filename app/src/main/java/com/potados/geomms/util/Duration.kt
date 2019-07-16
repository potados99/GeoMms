package com.potados.geomms.util

import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * 시간 간격을 나타냅니다.

 * @param millis:               밀리초로 나타낸 시간 간격.
 * @throws RuntimeException:    millis가 음수일 때.
 */
class Duration(private val millis: Long) {

    init {
        if (millis < 0) {
            throw RuntimeException("negative duration cannot exist.")
        }
    }

    /**
     * Duration을 밀리초로 표현합니다.
     */
    fun toMillis(): Long {
        return millis
    }

    fun toSeconds(): Long {
        return TimeUnit.MILLISECONDS.toSeconds(millis)
    }

    /**
     * Duration을 분으로 표현합니다.
     */
    fun toMinutes(): Long {
        return TimeUnit.MILLISECONDS.toMinutes(millis)
    }

    /**
     * Duration을 시간으로 표현합니다.
     */
    fun toHours(): Long {
        return TimeUnit.MILLISECONDS.toHours(millis)
    }

    /**
     * Duration을 분, 초, 밀리초로 나누어 표현합니다.
     */
    val minutePart = (millis / 1000) / 60
    val secondPart = (millis / 1000) % 60
    val millisPart = millis % 1000


    fun toShortenString(): String {
        return if (minutePart == 0L) secondPart.toString() + "초"
        else minutePart.toString() + "분"
    }

    override fun toString(): String {
        return toShortenString()
    }


    companion object {
        fun between(some: DateTime, another: DateTime): Duration {
            return Duration(abs(another.timeStamp - some.timeStamp))
        }
    }

}