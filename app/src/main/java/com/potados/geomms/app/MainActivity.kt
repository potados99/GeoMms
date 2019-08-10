package com.potados.geomms.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.potados.geomms.feature.location.presentation.MapFragment
import com.potados.geomms.feature.conversations.ConversationsFragment
import com.potados.geomms.R
import com.potados.geomms.common.base.NavigationActivity
import com.potados.geomms.common.base.NavigationFragment
import com.potados.geomms.common.extension.addAll
import com.potados.geomms.common.extension.inImmediateTransaction
import com.potados.geomms.common.extension.showOnly
import kotlinx.android.synthetic.main.main_activity.*

/**
 * 권한 획득과 기본 앱 설정 후 나타나는 주 액티비티입니다.
 */
class MainActivity : NavigationActivity() {

    private val _fragments by lazy{
        listOf(ConversationsFragment(), MapFragment())
    }

    override fun fragments(): List<NavigationFragment> = _fragments

    override fun menuResId(): Int = R.menu.bottom_nav_menu

    override fun defaultMenuItemId(): Int = R.id.menu_item_navigation_message

    companion object {
        fun callingIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}

