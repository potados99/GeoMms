package com.potados.geomms.extension

import com.potados.geomms.base.Failable
import timber.log.Timber

/**
 * Try something.
 * If nothing thrown, the evaluation of body is returned.
 * Else, set failure and return null.
 */
fun <T> Failable.nullOnFail(logOnError: Boolean = true, body: () -> T?): T? {
    return try {
        body()
    } catch (e: Exception) {
        if (logOnError) {
            Timber.w(e)
        }
        setFailure(Failable.Failure(e.message ?: "Unknown failure"))

        null
    }
}

/**
 * Try something.
 * if something thrown set failure.
 */
fun <T> Failable.unitOnFail(logOnError: Boolean = true, body: () -> T?) {
    try {
        body()
    } catch (e: Exception) {
        if (logOnError) {
            Timber.w(e)
        }
        setFailure(Failable.Failure(e.message ?: "Unknown failure"))
    }
}
