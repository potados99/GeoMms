/**
 * UseCase.kt
 *
 * Credits to Fernando Cejas.
 * https://github.com/android10/Android-CleanArchitecture-Kotlin
 */
package com.potados.geomms.interactor

import android.os.Handler
import android.os.Looper
import com.potados.geomms.functional.Result
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.concurrent.ThreadPoolExecutor
import kotlin.coroutines.suspendCoroutine

/**
 * Abstract class for Use Case (Interactor in terms of Clean Architecture).
 * Any use case in this application should implement this.
 */
abstract class UseCase<in Params> {
    abstract fun run(params: Params): Result<*>

    /**
     * Use thread instead of coroutine because it ruins Realm.
     */
    operator fun invoke(params: Params, onResult: (Result<*>) -> Unit = {}) {
        Thread {
            try {
                val result = run(params)
                Handler(Looper.getMainLooper()).post { onResult(result) }
            } catch (e: Exception) {
                Timber.w("Exception inside another thread.")
                Timber.w(e)
            }
        }.start()
    }
}