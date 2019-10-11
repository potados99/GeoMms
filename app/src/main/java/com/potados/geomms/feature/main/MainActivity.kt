/*
 * Copyright (C) 2019 Song Byeong Jun <potados99@gmail.com>
 *
 * This file is part of GeoMms.
 *
 * GeoMms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoMms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoMms.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.potados.geomms.feature.main

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import com.potados.geomms.BuildConfig
import com.potados.geomms.R
import com.potados.geomms.common.base.NavigationActivity
import com.potados.geomms.common.base.NavigationFragment
import com.potados.geomms.common.extension.getViewModel
import com.potados.geomms.common.extension.newBroadcastReceiver
import com.potados.geomms.common.extension.observe
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.databinding.MainActivityBinding
import com.potados.geomms.feature.conversations.ConversationsFragment
import com.potados.geomms.feature.license.LicenseActivity
import com.potados.geomms.feature.location.MapFragment
import com.potados.geomms.repository.SyncRepository
import com.potados.geomms.service.LocationSupportService
import com.potados.geomms.util.Notify
import kotlinx.android.synthetic.main.drawer_view.view.*
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_activity.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.system.exitProcess


class MainActivity : NavigationActivity(), KoinComponent {

    override val fragments: List<NavigationFragment> = listOf(ConversationsFragment(), MapFragment())

    override val navigationMenuId: Int = R.menu.bottom_nav
    override val defaultMenuItemId: Int = R.id.menu_item_navigation_map
    override val layoutId: Int? = null /* We use data binding here. */

    private val service: LocationSupportService by inject()
    private val navigator: Navigator by inject()

    private lateinit var viewDataBinding: MainActivityBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var toggle: ActionBarDrawerToggle

    private var lastTimeBackButtonPressed = 0L

    private val showMapReceiver = newBroadcastReceiver {
        it?.let { selectTab(R.id.menu_item_navigation_map) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        super.onCreate(savedInstanceState)

        viewModel = getViewModel()

        viewDataBinding.apply {
            lifecycleOwner = this@MainActivity
            vm = viewModel

            initializeView(root)
        }

        service.start()

        registerReceiver(showMapReceiver, IntentFilter(ACTION_SHOW_MAP))
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        viewModel.apply {
            observe(syncEvent) {
                if (it != SyncRepository.SyncEvent.EVENT_NONE) {
                    showSyncDialog(this@MainActivity)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(showMapReceiver)
    }

    override fun onBackPressed() {
        if (lastTimeBackButtonPressed != 0L &&
            System.currentTimeMillis() - lastTimeBackButtonPressed < 1000L) {
            // combo success
            exitProcess(0)
        } else {
            lastTimeBackButtonPressed = System.currentTimeMillis()
            Notify(this).short(R.string.notify_back_again_to_exit)
        }
    }

    private fun setDrawer(root: DrawerLayout) {
        toggle = ActionBarDrawerToggle(
            this,
            root,
            toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        toggle.syncState()
    }

    private fun setDrawerItemListeners(view: View) {
        setDrawerItemClickListener(view.header) {
            navigator.showGuides()
        }

        setDrawerItemClickListener(view.settings) {
            navigator.showSettings()
        }

        setDrawerItemClickListener(view.help, autoClose = false) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"

                putExtra(Intent.EXTRA_EMAIL, arrayOf("potados99@example.com"))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.title_troubleshooting))
                putExtra(Intent.EXTRA_TEXT, "")
            }

            try {
                startActivity(Intent.createChooser(intent, "Send mail..."))
            } catch (ex: android.content.ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "There are no email clients installed.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        setDrawerItemClickListener(view.rate, autoClose = true) {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.STORE_LINK))
            )
        }

        setDrawerItemClickListener(view.invite, autoClose = true) {
            navigator.showCompose(getString(R.string.message_get_geomms, BuildConfig.STORE_LINK))
        }

        setDrawerItemClickListener(view.oss_license) {
            startActivity(Intent(this, LicenseActivity::class.java))
        }
    }

    private fun initializeView(view: View) {
        setDrawer(view.root_layout)
        setDrawerItemListeners(view)
    }

    private fun setDrawerItemClickListener(item: View,  autoClose: Boolean = true, listener: (View) -> Unit) {
        item.setOnClickListener{
            listener(it)
            if (autoClose) {
                root_layout.closeDrawers()
            }
        }
    }

    companion object {
        fun callingIntent(context: Context) = Intent(context, MainActivity::class.java)

        const val ACTION_SHOW_MAP = "com.potados.geomms.SHOW_MAP"
    }
}

