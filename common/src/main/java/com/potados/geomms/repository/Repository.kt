package com.potados.geomms.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.potados.geomms.base.Failable
import com.potados.geomms.base.FailableComponent
import com.potados.geomms.base.Startable
import timber.log.Timber

/**
 * Base class of Repository defining default action of
 * initialization and error handling.
 */
abstract class Repository : FailableComponent(), Startable {
    override fun start() {
        // Do nothing
    }
}