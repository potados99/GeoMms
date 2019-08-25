/**
 * UseCase.kt
 *
 * Credits to Fernando Cejas.
 * https://github.com/android10/Android-CleanArchitecture-Kotlin
 */
package com.potados.geomms.interactor

import com.potados.geomms.functional.Result
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Abstract class for Use Case (Interactor in terms of Clean Architecture).
 * Any use case in this application should implement this.
 */
abstract class UseCase<in Params> {
    abstract suspend fun run(params: Params): Result<*>

    /**
     * Execute [run] in Global Scope co-routine and launch onResult on Main thread.
     */
    operator fun invoke(params: Params, onResult: (Result<*>) -> Unit = {}) {
        val job = GlobalScope.async { run(params) }

        MainScope().launch {
            onResult(job.await())
        }
    }
}