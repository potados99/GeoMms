package com.potados.geomms.core.platform

import android.os.Bundle
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.potados.geomms.R
import com.potados.geomms.core.extension.showOnly
import com.potados.geomms.core.platform.interfaces.HasBottomNavigation

/**
 * Base class for Activity with Bottom Navigation View.
 * Handle Fragment switch when the menu item is selected.
 */
abstract class NavigationBasedActivity : BaseActivity(),
    HasBottomNavigation /* navigationMenu(), navigationMenuId() */
{
    /**
     * Default layout for [NavigationBasedActivity].
     */
    override fun layoutId(): Int = R.layout.navigation_activity_layout

    /**
     * Switch fragment by only showing the selected one.
     */
    private val onNavigationItemChanged = { menuItem: MenuItem ->
        supportFragmentManager.showOnly {
            (it as NavigationBasedFragment).menuItemId() == menuItem.itemId
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!fragments().all { it is NavigationBasedFragment }) {
            throw RuntimeException("NavigationBasedActivity must have NavigationBasedFragment.")
        }

        super.onCreate(savedInstanceState)

        setNavigationView()
    }

    /**
     * Inflate Navigation menu and set listener.
     */
    private fun setNavigationView() {
        with(findViewById<BottomNavigationView>(navigationId())) {
            inflateMenu(navigationMenuId())

            if (fragments().isNotEmpty()) {
                /**
                 * listener is useless without fragments.
                 */
                setOnNavigationItemSelectedListener(onNavigationItemChanged)

                /**
                 * Set default selected menu.
                 */
                defaultNavigationItemId()?.let(::setSelectedItemId)
            }
        }
    }
}
