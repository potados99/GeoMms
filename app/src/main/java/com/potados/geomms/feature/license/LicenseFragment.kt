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

package com.potados.geomms.feature.license

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.extension.setSupportActionBar
import com.potados.geomms.model.License
import com.potados.geomms.util.RawReader
import kotlinx.android.synthetic.main.license_fragment.view.*

class LicenseFragment : BaseFragment() {
    private lateinit var adapter: LicenseAdapter

    init {
        failables += this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (context == null) return

        adapter = LicenseAdapter(context!!)

        val result = mutableListOf<License>()

        val metadata = RawReader(context!!, R.raw.third_party_license_metadata).toStringList().map { it.substringAfter(' ') }
        val license = RawReader(context!!, R.raw.third_party_licenses).toStringList()

        if (metadata.size != license.size) return

        for (i in metadata.indices) {
            result.add(License(metadata[i], license[i]))
        }

        adapter.data = result
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.license_fragment, container, false).apply {
            licenses.adapter = adapter
            setSupportActionBar(toolbar, title = false, upButton = true)
        }
    }
}