/**
 * UseCase.kt
 *
 * Credits to Fernando Cejas.
 * https://github.com/android10/Android-CleanArchitecture-Kotlin
 */
package com.potados.geomms.core.interactor

import com.potados.geomms.core.functional.Result
import kotlinx.coroutines.*
import kotlin.Result.Companion.success

/**
 * Abstract class for Use Case (Interactor in terms of Clean Architecture).
 * Any use case in this application should implement this.
 */
abstract class UseCase<out Type: Any, in Params> {
    abstract suspend fun run(params: Params): Result<Type>

    /**
     * Execute [run] in Global Scope co-routine and launch onResult on Main conversation.
     */
    operator fun invoke(params: Params, onResult: (Result<Type>) -> Unit = {}) {
        val job = GlobalScope.async { run(params) }

        MainScope().launch {
            onResult(job.await())
        }
    }

    class None
}