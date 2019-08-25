package com.potados.geomms.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Abstract class that defines failure handling.
 */
abstract class FailableComponent : Failable {
    private val failure = MutableLiveData<Failable.Failure>()

    final override fun setFailure(failure: Failable.Failure) {
        this.failure.value = failure
    }

    final override fun getFailure(): LiveData<Failable.Failure> {
        return failure
    }
}