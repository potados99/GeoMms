/**
 * Copyright (C) 2019 Song Byeong Jun and original authors
 *
 * This file is part of GeoMms.
 *
 * This software makes use of third-party patent which belongs to
 * KANG MOON KYOU and LEE GWI BONG:
 * System and Method for sharing service of location information
 * 10-1235884-0000 (2013.02.15)
 *
 * GeoMms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoMms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoMms.  If not, see <http://www.gnu.org/licenses/>.
 */

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

    protected val context: Context by inject()

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
        setFailure(Failable.Failure(context.getString(message, *formatArgs), show))
    }

    @CallSuper
    override fun start() {
        Timber.v("${this::class.java.name} started.")
    }
}