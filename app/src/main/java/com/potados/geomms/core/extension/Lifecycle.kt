/**
 * Lifecycle.kt
 *
 * Credits to Fernando Cejas.
 * https://github.com/android10/Android-CleanArchitecture-Kotlin
 */
package com.potados.geomms.core.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.potados.geomms.core.exception.Failure

fun <T: Any, L: LiveData<T>> LifecycleOwner.observe(liveData: L, body: (T?) -> Unit) =
    liveData.observe(this, Observer(body))

fun <L : LiveData<Exception>> LifecycleOwner.failure(liveData: L, body: (Exception?) -> Unit) =
    liveData.observe(this, Observer(body))