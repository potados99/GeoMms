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
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import com.potados.geomms.R
import com.potados.geomms.common.base.NavigationActivity
import com.potados.geomms.common.base.NavigationFragment
import com.potados.geomms.common.extension.getViewModel
import com.potados.geomms.common.extension.observe
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.feature.conversations.ConversationsFragment
import com.potados.geomms.feature.license.LicenseActivity
import com.potados.geomms.feature.location.MapFragment
import com.potados.geomms.repository.SyncRepository
import com.potados.geomms.service.LocationSupportService
import com.potados.geomms.util.Notify
import kotlinx.android.synthetic.main.drawer_view.*
import kotlinx.android.synthetic.main.main_activity.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.system.exitProcess

class MainActivity : NavigationActivity(), KoinComponent {

    override val fragments: List<NavigationFragment> = listOf(ConversationsFragment(), MapFragment())

    override val navigationMenuId: Int = R.menu.bottom_nav
    override val defaultMenuItemId: Int = R.id.menu_item_navigation_map
    override val layoutId: Int = R.layout.main_activity

    private val service: LocationSupportService by inject()
    private val navigator: Navigator by inject()

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var viewModel: MainViewModel

    private var lastTimeBackButtonPressed = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setDrawer()
        setService()

        navigator.showDefaultSmsDialogIfNeeded()

        viewModel = getViewModel()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        viewModel.apply {
            start()

            observe(syncEvent) {
                // Avoid duplicated sync.
                // Ignore sync event before first sync.
                val conditionToSync = (it == SyncRepository.SyncEvent.EVENT_INITIAL || !isNotSyncedYet())
                if (it != SyncRepository.SyncEvent.EVENT_NONE && conditionToSync) {
                    showSyncDialog(this@MainActivity)
                }
            }
        }
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

    private fun setDrawer() {
        toggle = ActionBarDrawerToggle(
            this,
            root_layout,
            toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        toggle.syncState()

        setDrawerItemListeners()
    }

    private fun setDrawerItemListeners() {
        setDrawerItemClickListener(header) {
            navigator.showGuides()
        }

        setDrawerItemClickListener(settings) {
            navigator.showSettings()
        }

        setDrawerItemClickListener(help, autoClose = false) {
            // TODO
            Notify(this).short(R.string.notify_not_implemented)
        }

        setDrawerItemClickListener(rate, autoClose = false) {
            // TODO
            Notify(this).short(R.string.notify_not_implemented)
        }

        setDrawerItemClickListener(invite, autoClose = false) {
            // TODO
            Notify(this).short(R.string.notify_not_implemented)
        }

        setDrawerItemClickListener(oss_license) {
            startActivity(Intent(this, LicenseActivity::class.java))
        }
    }


    /**
     * Until user tab the Map tab, this service does not get started.
     * So do it here manually.
     */
    private fun setService() {
        service.start()
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
    }
}

