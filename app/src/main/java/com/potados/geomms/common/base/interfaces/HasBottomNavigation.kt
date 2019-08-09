package com.potados.geomms.common.base.interfaces

/**
 * Indicate that the child has toolbar.
 */
interface HasBottomNavigation {
    fun navigationId(): Int
    fun navigationMenuId(): Int
    fun defaultNavigationItemId(): Int?
}