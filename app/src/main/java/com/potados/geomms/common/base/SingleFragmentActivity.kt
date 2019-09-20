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

package com.potados.geomms.common.base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.potados.geomms.R
import com.potados.geomms.common.extension.inImmediateTransaction

abstract class SingleFragmentActivity : BaseActivity() {

    abstract val fragment: Fragment
    open val layoutId: Int = R.layout.single_fragment_activity

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)

        addFragment(savedInstanceState)
    }

    private fun addFragment(savedInstanceState: Bundle?) =
        savedInstanceState ?:
        supportFragmentManager.inImmediateTransaction {
            add(R.id.fragment_container, fragment)
            this
        }
}