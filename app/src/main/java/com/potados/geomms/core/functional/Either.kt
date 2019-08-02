/**
 * Either.kt
 *
 * Credits to Fernando Cejas.
 * https://github.com/android10/Android-CleanArchitecture-Kotlin
 */
package com.potados.geomms.core.functional

/**
 * Represents a value of one of two possible types.
 * Instance of [Either] are either an instance of [Left] or [Right].
 * Conventionally [Left] is used for "failure"
 * and [Right] is used for "success".
 */
sealed class Either<out L, out R> {
    data class Left<out L>(val a: L) : Either<L, Nothing>()
    data class Right<out R>(val b: R) : Either<Nothing, R>()

    val isLeft get() = this is Left<L>
    val isRight get() = this is Right<R>

    val left: L get() = if (this is Left) a else throw IllegalAccessError()
    val right: R get() = if (this is Right) b else throw IllegalAccessError()

    fun <L> left(a: L) = Left(a)
    fun <R> right(b: R) = Right(b)

    fun either(fnL: (L) -> Any, fnR: (R) -> Any): Any =
        when (this) {
            is Left -> fnL(a)
            is Right -> fnR(b)
        }
}

/**
 * Pipeline function.
 * From A to C using A -> B and B -> C.
 */
fun <A, B, C> ((A) -> B).and(fn: (B) -> C): (A) -> C = {
    fn(this(it /* A */) /* B */) /* C */
}

/**
 * Create a flat map.
 * Return type of [transform] must be same as [this].
 */
fun <T, L, R> Either<L, R>.flatMap(transform: (R) -> Either<L, T>): Either<L, T> =
    when (this) {
        is Either.Left -> Either.Left(a) /* new instance */
        is Either.Right -> transform(b)
    }

/**
 * Return a flatMap after applying [transform].
 *
 * [map] passes the function which does:
 * 1. run [transform].
 * 2. pass the result of 1 to [right].
 * 3. return the result of 2.
 * to [flatMap].
 *
 * So it will look like { right(transform(it)) }.
 * transform.and(::right) == { right(transform(it)) } is true.
 */
fun <T, L, R> Either<L, R>.map(transform: (R) -> (T)): Either<L, T> =
    this.flatMap(transform.and(::right))