package com.potados.geomms.core.platform.interfaces

/**
 * Indicate that the child has toolbar.
 */
interface HasBottomNavigation {
    fun navigationId(): Int
    fun navigationMenuId(): Int
    fun defaultNavigationItemId(): Int?
}