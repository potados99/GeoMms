package com.potados.geomms.extension

import com.potados.geomms.base.Failable
import timber.log.Timber

/**
 * Run code block.
 * Set failure if any exceptions are thrown.
 *
 * @return null if any exceptions.
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
 * Run code block.
 * Set failure if any exceptions.
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

/**
 * Run code block.
 * Set failure if any exceptions are thrown.
 *
 * @return false if any exceptions.
 */
fun Failable.falseOnFail(logOnError: Boolean = true, body: () -> Boolean): Boolean {
    return try {
        body()
    } catch (e: Exception) {
        if (logOnError) {
            Timber.w(e)
        }
        setFailure(Failable.Failure(e.message ?: "Unknown failure"))

        false
    }
}
