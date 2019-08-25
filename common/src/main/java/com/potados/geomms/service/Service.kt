package com.potados.geomms.service

import com.potados.geomms.base.FailableComponent
import com.potados.geomms.base.Startable
import timber.log.Timber

/**
 * Base class of Service defining default action of
 * initialization and error handling.
 */
abstract class Service : FailableComponent(), Startable {
    override fun start() {
        Timber.v("${this::class.java.name} started.")
    }
}