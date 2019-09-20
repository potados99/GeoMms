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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.model.License
import kotlinx.android.synthetic.main.license_list_item.view.*


class LicenseAdapter(private val context: Context) : BaseAdapter<License>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.license_list_item, parent, false)

        return BaseViewHolder(view)
    }
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = getItem(position) ?: return
        with(holder.containerView) {
            package_name.text = item.packageName
            license_url.text = item.licenseUrl

            setOnClickListener {
                Intent(Intent.ACTION_VIEW).run {
                    data = Uri.parse(item.licenseUrl)
                    context.startActivity(this)
                }
            }
        }
    }
}