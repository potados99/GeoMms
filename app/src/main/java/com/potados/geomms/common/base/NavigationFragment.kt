package com.potados.geomms.common.base

import android.os.Bundle
import android.view.View
import com.potados.geomms.common.extension.setTitle

abstract class NavigationFragment : BaseFragment() {
    abstract val navigationItemId: Int
    abstract val titleId: Int

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle(context?.getString(titleId))
    }

    /**
     * Called after this fragment is shown
     */
    open fun onShow() {}

    /**
     * Called after this fragment is hidden
     */
    open fun onHide() {}
}