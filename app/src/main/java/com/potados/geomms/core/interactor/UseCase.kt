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
 * Abstract class for a Use Case (Interactor in terms of Clean Architecture).
 * This abstraction represents an execution unit for different use cases (this means than any use
 * case in the application should implement this contract).
 *
 * By convention each [UseCase] implementation will execute its job in a background thread
 * (kotlin co-routine) and will post the result in the UI thread.
 */
abstract class UseCase<out Type: Any, in Params> {
    abstract suspend fun run(params: Params): Either<Failure, Type>

    /**
     * Execute [run] in Global Scope co-routine and launch onResult on Main thread.
     */
    operator fun invoke(params: Params, onResult: (Either<Failure, Type>) -> Unit = {}) {
        MainScope().launch {
            onResult(withContext(GlobalScope.coroutineContext) { run(params) })
        }
    }



}