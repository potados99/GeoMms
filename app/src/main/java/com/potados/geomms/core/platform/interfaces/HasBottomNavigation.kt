package com.potados.geomms.core.platform.interfaces

import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Indicate that the child has toolbar.
 */
interface HasBottomNavigation {
    fun navigationMenu(): BottomNavigationView
    fun navigationMenuId(): Int
}