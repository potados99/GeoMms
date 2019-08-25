package com.potados.geomms.common.base

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.potados.geomms.R
import com.potados.geomms.base.Failable
import com.potados.geomms.base.FailableHandler
import com.potados.geomms.common.extension.notify
import com.potados.geomms.common.extension.observe
import com.potados.geomms.common.extension.resolveThemeColor
import com.potados.geomms.common.extension.setTint
import timber.log.Timber

/**
 * Base Fragment that has options menu and failure handling.
 */
abstract class BaseFragment : Fragment(), FailableHandler {
    open val optionMenuId: Int? = null

    private var menu: Menu? = null
    fun getOptionsMenu(): Menu? = menu

    @CallSuper
    override fun onFail(failure: Failable.Failure) {
        notify(failure.message, long = true)
        Timber.w("Failure with message: $failure")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(optionMenuId != null)
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

    final override fun addFailables(failables: List<Failable>) {
        failables.forEach { failable ->
            // remove before observe to prevent double observing.
            failable.getFailure().removeObservers(this)
            observe(failable.getFailure()) { failure -> failure?.let(::onFail) }
        }
    }
}