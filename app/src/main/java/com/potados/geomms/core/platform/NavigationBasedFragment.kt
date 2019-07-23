package com.potados.geomms.core.platform

/**
 * Base class for Fragment which is a member of [NavigationBasedActivity].
 */
abstract class NavigationBasedFragment : BaseFragment() {

    /**
     * Id of menu item where this Fragment is mapped.
     */
    abstract fun menuItemId(): Int
}