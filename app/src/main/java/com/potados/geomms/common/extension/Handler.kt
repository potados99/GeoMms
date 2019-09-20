package com.potados.geomms.common.extension

import android.os.Handler

fun <T> Handler.doAfter(delayMillis: Long, body: () -> T?) {
    postDelayed({ body() }, delayMillis)
}