/**
 * BaseViewModel.kt
 *
 * Credits to Fernando Cejas.
 * https://github.com/android10/Android-CleanArchitecture-Kotlin
 */
package com.potados.geomms.core.platform

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.potados.geomms.core.exception.Failure

/**
 * ViewModel with default Failure handling.
 */
abstract class BaseViewModel : ViewModel() {

    val failure = MutableLiveData<Failure>()

    protected fun handleFailure(failure: Failure) {
        this.failure.value = failure
    }

}