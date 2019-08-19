package com.potados.geomms.common.base

import androidx.fragment.app.Fragment

abstract class NavigationFragment : BaseFragment() {
    abstract fun navigationMenuId(): Int
}