package com.potados.geomms.common.base

import android.content.Context
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.potados.geomms.base.Failable
import com.potados.geomms.base.FailableContainer
import com.potados.geomms.base.Startable
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

/**
 * Base View Model that can handle failure inside it.
 */
abstract class BaseViewModel : ViewModel(), Startable, Failable, FailableContainer, KoinComponent {

    private val mContext: Context by inject()

    /**
     * Failure of View Model itself
     */
    private val failure = MutableLiveData<Failable.Failure>()

    /**
     * Failable properties inside this View Model
     */
    override val failables: MutableList<Failable> = mutableListOf()

    final override fun setFailure(failure: Failable.Failure) {
        this.failure.postValue(failure)
        Timber.w("Failure is set: ${failure.message}")
    }

    final override fun getFailure(): LiveData<Failable.Failure> {
        return failure
    }

    override fun fail(@StringRes message: Int, vararg formatArgs: Any?, show: Boolean) {
        setFailure(Failable.Failure(mContext.getString(message, *formatArgs), show))
    }

    @CallSuper
    override fun start() {
        Timber.v("${this::class.java.name} started.")
    }
}