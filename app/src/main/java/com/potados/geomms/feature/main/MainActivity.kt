package com.potados.geomms.feature.main

import android.content.Context
import android.content.Intent
import com.potados.geomms.R
import com.potados.geomms.feature.location.MapFragment
import com.potados.geomms.feature.conversations.ConversationsFragment
import com.potados.geomms.common.base.NavigationActivity
import com.potados.geomms.common.base.NavigationFragment
import timber.log.Timber

/**
 * 권한 획득과 기본 앱 설정 후 나타나는 주 액티비티입니다.
 */
class MainActivity : NavigationActivity() {

    private val _fragments= listOf(ConversationsFragment(), MapFragment())

    override fun fragments(): List<NavigationFragment> = _fragments

    override fun navigationMenuId(): Int = R.menu.bottom_nav

    override fun defaultMenuItemId(): Int = R.id.menu_item_navigation_message

    override fun invalidateOptionsMenu() {
        super.invalidateOptionsMenu()
        Timber.i("options menu invalidated")
    }

    companion object {
        fun callingIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}

