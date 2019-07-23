package com.potados.geomms.core.platform

import android.os.Bundle
import android.view.MenuItem
import com.potados.geomms.R
import com.potados.geomms.core.extension.showOnly
import kotlinx.android.synthetic.main.navigation_activity_layout.*

/**
 * Base class for Activity with Bottom Navigation View.
 * Handle Fragment switch when the menu item is selected.
 */
abstract class NavigationBasedActivity : BaseActivity() {

    /**
     * Id of Navigation Menu to inflate.
     */
    abstract fun navigationMenuResId(): Int

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

        setContentView(R.layout.navigation_activity_layout)
        setNavigationView()
    }

    /**
     * Inflate Navigation menu and set listener.
     */
    private fun setNavigationView() {
        nav_view.inflateMenu(navigationMenuResId())

        if (fragments().isNotEmpty()) {
            nav_view.setOnNavigationItemSelectedListener(onNavigationItemChanged)
        }
    }
}
