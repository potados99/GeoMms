package com.potados.geomms.extension

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun <R> elapsedTimeMillis(action: () -> R): Long {
    val startTime = System.currentTimeMillis()
    action()
    val endTime = System.currentTimeMillis()

    return endTime - startTime
}

inline fun <T, R> withNonNull(receiver: T?, block: T.() -> R): R? {
    return if (receiver == null) null
    else with(receiver, block)
}