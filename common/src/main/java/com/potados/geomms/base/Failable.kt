package com.potados.geomms.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Represents a component that can fail.
 */
interface Failable {
    /**
     * Get failure Live Data.
     */
    fun getFailure(): LiveData<Failure>

    /**
     * Set failure Live Data.
     */
    fun setFailure(failure: Failure)

    data class Failure(val message: String)
}