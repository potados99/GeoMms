package com.potados.geomms.core.platform

import android.os.Bundle
import android.view.MenuItem
import com.potados.geomms.R
import com.potados.geomms.core.extension.inTransaction
import kotlinx.android.synthetic.main.tab_activity_layout.*

abstract class TabBasedActivity : BaseActivity() {

    abstract fun navigationMenuLayoutId(): Int

    private val onNavigationItemChanged = { menuItem: MenuItem ->
        val selectedFragment = fragments().find {
            (it as TabBasedFragment).menuId() == menuItem.itemId
        }

        selectedFragment?.let { selected ->
            supportFragmentManager.inTransaction {
                fragments().forEach { hide(it) }
                show(selected)
            }
            true
        } ?: false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!fragments().all { it is TabBasedFragment }) {
            throw RuntimeException("TabBasedActivity must have TabBasedFragment.")
        }

        super.onCreate(savedInstanceState)

        setContentView(R.layout.tab_activity_layout)
        setNavigationView()
    }

    private fun setNavigationView() {
        nav_view.inflateMenu(navigationMenuLayoutId())

        if (fragments().isNotEmpty()) {
            nav_view.setOnNavigationItemSelectedListener(onNavigationItemChanged)
        }
    }
}
