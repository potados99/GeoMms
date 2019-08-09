package com.potados.geomms.app

import android.content.Context
import android.content.Intent
import com.potados.geomms.feature.location.representation.MapFragment
import com.potados.geomms.feature.conversations.ConversationsFragment
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.base.NavigationBasedActivity

/**
 * 권한 획득과 기본 앱 설정 후 나타나는 주 액티비티입니다.
 */
class MainActivity : NavigationBasedActivity() {

    /**
     * NavigationBasedActivity 설정들.
     */
    override fun toolbarId(): Int? = null
    override fun toolbarMenuId(): Int? = null
    override fun fragments(): Array<out BaseFragment> = mFragments
    override fun navigationId(): Int = R.id.nav_view
    override fun navigationMenuId(): Int = R.menu.bottom_nav_menu
    override fun defaultNavigationItemId(): Int? = R.id.menu_item_navigation_message

    /** 사용할 프래그먼트들 */
    private val mFragments by lazy{
        arrayOf(ConversationsFragment(), MapFragment())
    }

    companion object {
        fun callingIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}

