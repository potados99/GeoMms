package com.potados.geomms.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber

/**
 * Abstract class that defines failure handling.
 */
abstract class FailableComponent : Failable {
    private val failure = MutableLiveData<Failable.Failure>()

    final override fun setFailure(failure: Failable.Failure) {
        this.failure.postValue(failure)
        Timber.w("Failure is set: ${failure.message}")
    }

    final override fun getFailure(): LiveData<Failable.Failure> {
        return failure
    }
}