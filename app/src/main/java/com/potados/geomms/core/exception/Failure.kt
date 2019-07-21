/**
 * Failure.kt
 *
 * Credits to Fernando Cejas.
 * https://github.com/android10/Android-CleanArchitecture-Kotlin
 */
package com.potados.geomms.core.exception

/**
 * Base class for handling errors.
 * Every feature specific failure should extend [FeatureFailure] class.
 */
sealed class Failure {
    object NetworkConnection : Failure()
    object LocationService : Failure()

    abstract class FeatureFailure : Failure()
}