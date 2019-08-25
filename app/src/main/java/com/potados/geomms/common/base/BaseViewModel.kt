package com.potados.geomms.common.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.potados.geomms.base.Failable
import com.potados.geomms.base.FailableContainer
import com.potados.geomms.base.Startable
import timber.log.Timber

/**
 * Base View Model that can handle failure inside it.
 */
abstract class BaseViewModel : ViewModel(), Startable, Failable, FailableContainer {

    /**
     * Failure of View Model itself
     */
    private val failure = MutableLiveData<Failable.Failure>()

    /**
     * Failable properties inside this View Model
     */
    override val failables: MutableList<Failable> = mutableListOf()

    final override fun setFailure(failure: Failable.Failure) {
        this.failure.value = failure
        Timber.v("A failure is set: ${failure.message}")
    }

    final override fun getFailure(): LiveData<Failable.Failure> {
        return failure
    }

    override fun start() {
        Timber.v("${this::class.java.name} started.")
    }
}