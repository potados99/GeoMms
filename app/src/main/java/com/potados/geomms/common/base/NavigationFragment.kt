package com.potados.geomms.common.base

import androidx.fragment.app.Fragment

abstract class NavigationFragment : Fragment() {
    abstract fun menuId(): Int
}