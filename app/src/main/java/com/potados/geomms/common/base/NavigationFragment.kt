package com.potados.geomms.common.base

import timber.log.Timber


abstract class NavigationFragment : BaseFragment() {
    abstract fun navigationItemId(): Int

    /**
     * Called after this fragment is shown
     */
    open fun onShow() {}

    /**
     * Called after this fragment is hidden
     */
    open fun onHide() {}
}