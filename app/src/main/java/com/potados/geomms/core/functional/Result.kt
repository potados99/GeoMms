package com.potados.geomms.core.functional

/**
 * A generic class that holds a value with its loading status.
 */
sealed class Result<out T> {

    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<None>()

    val succeeded get() = this is Success && data != null

    fun onSuccess(body: (T) -> Any?): Result<*> {
        if (this is Success && data != null) {
            body(data)
        }
        return this
    }

    fun onError(body: (Exception) -> Any?): Result<*> {
        if (this is Error) {
            body(exception)
        }
        return this
    }

    fun <R> either(onSuccess: (T) -> R, onError: (Exception) -> R): R =
        when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(exception)
        }

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }
}
