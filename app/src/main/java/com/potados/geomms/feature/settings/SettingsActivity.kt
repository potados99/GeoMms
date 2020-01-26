/*
 * Copyright (C) 2019-2020 Song Byeong Jun <potados99@gmail.com>
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

package com.potados.geomms.feature.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.potados.geomms.R
import com.potados.geomms.common.base.SingleFragmentActivity
import com.potados.geomms.extension.withNonNull
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : SingleFragmentActivity() {

    override val fragment: Fragment = SettingsFragment()
    override val layoutId: Int = R.layout.settings_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView()
    }

    private fun initializeView() {
        setSupportActionBar(toolbar)

        // Here, activity has the toolbar.
        withNonNull(supportActionBar) {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)

            toolbar_title.text = getString(R.string.title_settings)
        }
    }

    companion object {
        fun callingIntent(context: Context) = Intent(context, SettingsActivity::class.java)
    }
}