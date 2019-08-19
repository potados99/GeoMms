package com.potados.geomms.common.base

import android.os.Bundle
import android.view.MenuItem
import com.potados.geomms.R
import com.potados.geomms.common.extension.addAll
import com.potados.geomms.common.extension.inImmediateTransaction
import com.potados.geomms.common.extension.inTransaction
import com.potados.geomms.common.extension.showOnly
import kotlinx.android.synthetic.main.navigation_activity.*

abstract class NavigationActivity : BaseActivity() {

    abstract fun fragments(): List<NavigationFragment>
    abstract fun navigationMenuId(): Int
    open fun defaultMenuItemId(): Int = -1

    private val onNavigationItemChanged = { menuItem: MenuItem ->
        supportFragmentManager.showOnly {
            (it as NavigationFragment).navigationItemId() == menuItem.itemId
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navigation_activity)

        addFragments(savedInstanceState)
        setNavigationView()
    }

    private fun addFragments(savedInstanceState: Bundle?) {
        savedInstanceState ?:
        supportFragmentManager.inImmediateTransaction {
            addAll(R.id.fragment_container, fragments())
            this
        }
    }

    private fun setNavigationView() {
        with(nav_view) {
            inflateMenu(navigationMenuId())

            setOnNavigationItemSelectedListener(onNavigationItemChanged)

            defaultMenuItemId().takeIf { it > 0 }?.let(::setSelectedItemId)
        }
    }
}