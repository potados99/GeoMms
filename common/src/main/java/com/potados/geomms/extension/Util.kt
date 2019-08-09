package com.potados.geomms.extension

fun <R> elapsedTimeMillis(action: () -> R): Long {
    val startTime = System.currentTimeMillis()
    action()
    val endTime = System.currentTimeMillis()

    return endTime - startTime
}