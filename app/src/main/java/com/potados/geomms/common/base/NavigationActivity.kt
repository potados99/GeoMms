/**
 * Copyright (C) 2019 Song Byeong Jun and original authors
 *
 * This file is part of GeoMms.
 *
 * This software makes use of third-party patent which belongs to
 * KANG MOON KYOU and LEE GWI BONG:
 * System and Method for sharing service of location information
 * 10-1235884-0000 (2013.02.15)
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

package com.potados.geomms.common.base

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.potados.geomms.R
import com.potados.geomms.common.extension.findFragmentByNavigationId
import com.potados.geomms.common.extension.setTitle
import com.potados.geomms.extension.withNonNull
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.navigation_activity.nav_view
import timber.log.Timber

/**
 * Base class for Bottom Navigation View based activity,
 * providing quick transition between fragments, without state loss.
 * Fragments are only shown or hidden when tab is switched.
 * They are lazy-added to Fragment Manager on demand.
 * It has customized Toolbar. @see [toolbar]
 *
 * Usage:
 * 1. Override [fragments], which will be used as tab contents.
 * 2. Override [navigationMenuId], the id of [BottomNavigationView].
 * 3. (Optional) Override [defaultMenuItemId] and/or [layoutId].
 */
abstract class NavigationActivity : BaseActivity() {

    abstract val fragments: List<NavigationFragment>

    abstract val navigationMenuId: Int
    open val defaultMenuItemId: Int = -1
    open val layoutId: Int = R.layout.navigation_activity

    private var activeFragmentId: Int = -1

    private val onNavigationItemChanged = { menuItem: MenuItem ->
        addOrShowFragment(menuItem.itemId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
        setToolbar()
        savedInstanceState ?: addOrShowFragment(defaultMenuItemId)
        setNavigationView()
    }

    private fun setToolbar() {
        setSupportActionBar(toolbar)
        withNonNull(supportActionBar) {
            setDisplayShowTitleEnabled(false)   // use title text sheetView instead.
            setDisplayHomeAsUpEnabled(false)    // no up button
        }
    }

    /**
     * Show only childFragment that has [id] as NavigationItemId.
     * If the destination childFragment is not added to childFragment manager, add it.
     *
     * @param id is navigation menu id of childFragment.
     * An id and a childFragment should consist a pair.
     *
     * @return false if [id] has no paired childFragment
     * or Fragment Manager has childFragment other than [NavigationFragment].
     */
    private fun addOrShowFragment(id: Int): Boolean {
        val transaction = supportFragmentManager.beginTransaction()

        try {
            // Ensure destination childFragment is added
            if (supportFragmentManager.findFragmentByNavigationId(id) == null) {
                val fragmentToAdd = fragments.find { it.navigationItemId == id } as? Fragment
                    ?: throw IllegalArgumentException("childFragment of corresponding id $id not exist.")

                transaction.add(R.id.fragment_container, fragmentToAdd)
                Timber.i("add new childFragment of id $id")
            }

            if (activeFragmentId == id) {
                return true
            }

            // Show only destination childFragment
            // Do this only when destination childFragment is not a
            // currently active childFragment
            supportFragmentManager.fragments.forEach {
                    // ensure all fragments are NavigationFragment
                    if (it !is NavigationFragment) throw RuntimeException("only NavigationFragment is allowed in NavigationActivity.")

                    if (it.navigationItemId == id) {
                        it.setTitle(getString(it.titleId))
                        transaction.show(it)
                        it.onShow()
                    } else {
                        transaction.hide(it)
                        it.onHide()
                    }
                }

            activeFragmentId = id

            return true
        } catch (e: Throwable) {
            Timber.w(e)
            return false
        } finally {
            // commit() does not add childFragment immediately.
            // It makes problem when calling [addOrShowFragment] rapidly
            // because it does not ensure childFragment is added after the call.
            // So the addition can occur over one time, which throws exception.
            // Use commitNow instead.
            transaction.commitNow()
        }
    }

    private fun setNavigationView() {
        with(nav_view) {
            inflateMenu(navigationMenuId)

            setOnNavigationItemSelectedListener(onNavigationItemChanged)

            defaultMenuItemId.takeIf { it > 0 }?.let(::setSelectedItemId)
        }
    }
}