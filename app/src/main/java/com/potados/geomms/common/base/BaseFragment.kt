package com.potados.geomms.common.base

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import androidx.fragment.app.Fragment
import com.potados.geomms.R
import com.potados.geomms.common.extension.resolveThemeColor
import com.potados.geomms.common.extension.setTint
import timber.log.Timber

abstract class BaseFragment : Fragment() {
    open fun optionMenuId(): Int? = null

    private var menu: Menu? = null
    fun getOptionsMenu(): Menu? = menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(optionMenuId() != null)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        optionMenuId()?.let {
            inflater.inflate(it, menu)
            Timber.d("inflate option menu")
        }

        context?.let {
            menu.setTint(it, it.resolveThemeColor(R.attr.tintPrimary))
        }

        this.menu = menu
    }
}