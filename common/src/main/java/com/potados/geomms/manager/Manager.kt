package com.potados.geomms.manager

import com.potados.geomms.base.FailableComponent
import com.potados.geomms.base.Startable
import timber.log.Timber

/**
 * Base class of Manager defining default action of
 * initialization and error handling.
 */
abstract class Manager : FailableComponent(), Startable {
    override fun start() {
        Timber.v("${this::class.java.name} started.")
    }
}