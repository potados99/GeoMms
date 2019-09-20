package com.potados.geomms.common.extension

import java.util.*

fun Timer.doEvery(interval: Long, delay: Long = 0L, body: () -> Unit) {
    schedule(object: TimerTask() {
        override fun run() {
            body()
        }
    }, delay, interval)
}