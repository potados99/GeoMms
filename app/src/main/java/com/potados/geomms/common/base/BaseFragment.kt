/*
 * Copyright (C) 2019-2020 Song Byeong Jun <potados99@gmail.com>
 *
 * This file is part of GeoMms.
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
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.potados.geomms.R
import com.potados.geomms.base.Failable
import com.potados.geomms.base.FailableContainer
import com.potados.geomms.base.FailableHandler
import com.potados.geomms.common.extension.notify
import com.potados.geomms.common.extension.observe
import com.potados.geomms.common.extension.resolveThemeColor
import com.potados.geomms.common.extension.setTint
import com.potados.geomms.common.manager.BottomSheetManager
import com.potados.geomms.common.manager.BottomSheetManagers
import com.potados.geomms.preference.MyPreferences
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

/**
 * Base Fragment that has options menu and failure handling.
 */
abstract class BaseFragment : Fragment(), Failable, FailableContainer, FailableHandler, KoinComponent {

    private val mContext: Context by inject()
    private val preferences: MyPreferences by inject()
    private val bottomSheetManagers: BottomSheetManagers by inject()

    open val optionMenuId: Int? = null

    private var menu: Menu? = null
    fun getOptionsMenu(): Menu? = menu

    /**
     * This is launched along with onViewCreated(sheetView: View, savedInstanceState: Bundle?).
     * It enable other components to listen to the status of this childFragment.
     */
    var onViewCreated: (View) -> Unit = {}

    /**
     * The bottom sheet manager we are associated with.
     */
    val bottomSheetManager: BottomSheetManager?
        get() {
            return bottomSheetManagers.find(this)
        }

    /**
     * Private bottom sheet manager for child bottom sheets inside of this one.
     */
    val childBottomSheetManager: BottomSheetManager?
        get() {
            return bottomSheetManagers.get(this)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(optionMenuId != null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (view is ViewGroup) {
            // Add child bottom sheet manager.
            bottomSheetManagers.add(BottomSheetManager(this, view))
        }

        onViewCreated(view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Observing failables in container is auto.
        // We can add failables in init.
        // Adding itself is recommended.
        startObservingFailables(failables)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Remove child bottom sheet manager.
        bottomSheetManagers.remove(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        optionMenuId?.let {
            inflater.inflate(it, menu)
            Timber.d("Inflate option menu")
        }

        context?.let {
            menu.setTint(it, it.resolveThemeColor(R.attr.tintPrimary))
        }

        this.menu = menu
    }

    override fun onDestroy() {
        super.onDestroy()

        stopObservingFailables()
    }

    /******************************
     * AS A Failable
     ******************************/
    private val failure = MutableLiveData<Failable.Failure>()
    override fun getFailure(): MutableLiveData<Failable.Failure> = failure
    override fun setFailure(failure: Failable.Failure) {
        this.failure.postValue(failure)
        Timber.w("Failure is set: ${failure.message}")
    }
    override fun fail(@StringRes message: Int, vararg formatArgs: Any?, show: Boolean) {
        setFailure(Failable.Failure(mContext.getString(message, *formatArgs), show))
    }

    /******************************
     * AS A FailableContainer
     ******************************/
    override val failables: MutableList<Failable> = mutableListOf()

    /******************************
     * AS A FailableHandler
     ******************************/
    override val observedFailables: MutableList<Failable> = mutableListOf()
    @CallSuper override fun onFail(failure: Failable.Failure) {
        if (failure.show || preferences.showAllError) {
            notify(failure.message, long = failure.show)
        }

        Timber.w("Failure with message: $failure")
    }
    final override fun startObservingFailables(failables: List<Failable>) {
        failables.forEach {
            // remove before observe to prevent double observing.
            it.getFailure().removeObservers(this)
            observe(it.getFailure()) { failure ->
                failure?.let(::onFail)

                // The failure is handled.
                it.getFailure().postValue(null)
            }
            observedFailables.add(it)
        }

        Timber.i("Started observing of ${failables.joinToString{ it::class.java.name }}.")
    }
    final override fun stopObservingFailables() {
        observedFailables.forEach {
            it.getFailure().removeObservers(this)
        }
        observedFailables.clear()

        Timber.i("Stopped observing of ${observedFailables.joinToString{ it::class.java.name }}.")
    }
}