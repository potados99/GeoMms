package com.potados.geomms.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.potados.geomms.feature.location.representation.MapFragment
import com.potados.geomms.feature.conversations.ConversationsFragment
import com.potados.geomms.R
import com.potados.geomms.common.base.NavigationBasedFragment
import com.potados.geomms.common.extension.addAll
import com.potados.geomms.common.extension.inImmediateTransaction
import com.potados.geomms.common.extension.showOnly
import kotlinx.android.synthetic.main.main_activity.*

/**
 * 권한 획득과 기본 앱 설정 후 나타나는 주 액티비티입니다.
 */
class MainActivity : AppCompatActivity() {

    /** 사용할 프래그먼트들 */
    private val fragments by lazy{
        arrayOf(ConversationsFragment(), MapFragment())
    }

    /**
     * Switch fragment by only showing the selected one.
     */
    private val onNavigationItemChanged = { menuItem: MenuItem ->
        supportFragmentManager.showOnly {
            (it as NavigationBasedFragment).menuItemId() == menuItem.itemId
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addFragments(savedInstanceState)
        setNavigationView()
    }

    /**
     * Add all fragments to [supportFragmentManager].
     */
    private fun addFragments(savedInstanceState: Bundle?) =
        savedInstanceState ?:
        supportFragmentManager.inImmediateTransaction {
            addAll(fragmentContainerId(), fragments())
            this
        }

    /**
     * Inflate Navigation menu and set listener.
     */
    private fun setNavigationView() {
        with(nav_view) {
            inflateMenu(R.menu.bottom_nav_menu)

            if (fragments.isNotEmpty()) {
                /**
                 * listener is useless without fragments.
                 */
                setOnNavigationItemSelectedListener(onNavigationItemChanged)

                /**
                 * Set default selected menu.
                 */
                selectedItemId = R.id.menu_item_navigation_message)
            }
        }
    }

    companion object {
        fun callingIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}

