package com.potados.geomms.common.manager

import androidx.fragment.app.Fragment
import com.potados.geomms.common.base.BaseFragment

class BottomSheetManagers {
    private val managers = HashMap<BaseFragment, BottomSheetManager>()

    fun add(manager: BottomSheetManager) {
        managers[manager.parentFragment()] = manager
    }

    fun remove(parentFragment: BaseFragment) {
        managers.remove(parentFragment)
    }

    fun get(parentFragment: BaseFragment): BottomSheetManager? {
        return managers[parentFragment]
    }

    /**
     * Find a bottom sheet manager that has [oneOfChildFragments] in stack.
     */
    fun find(oneOfChildFragments: BaseFragment): BottomSheetManager? {
        return managers.values.find { it.findSheetByFragment(oneOfChildFragments) != null }
    }
}