/**
 * UseCase.kt
 *
 * Credits to Fernando Cejas.
 * https://github.com/android10/Android-CleanArchitecture-Kotlin
 */
package com.potados.geomms.core.interactor

import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.functional.Either
import kotlinx.coroutines.*

/**
 * Abstract class for Use Case (Interactor in terms of Clean Architecture).
 * Any use case in this application should implement this.
 */
abstract class UseCase<out Type: Any, in Params> {
    abstract suspend fun run(params: Params): Either<Failure, Type>

    /**
     * Execute [run] in Global Scope co-routine and launch onResult on Main thread.
     */
    operator fun invoke(params: Params, onResult: (Either<Failure, Type>) -> Unit = {}) {
        val job = GlobalScope.async { run(params) }

        MainScope().launch {
            onResult(job.await())
        }
    }

    class None
}