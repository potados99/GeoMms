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

package com.potados.geomms.common

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseActivity
import com.potados.geomms.common.extension.resolveThemeColor
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.injection.permissions
import com.potados.geomms.util.Popup
import kotlinx.android.synthetic.main.permission_activity.*
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * Explain user why we need these permissions.
 * After that, make a permission request to system.
 */
class GiveMePermissionActivity: BaseActivity() {

    private val navigator: Navigator by inject()
    private val permissionManager: com.potados.geomms.manager.PermissionManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (permissionManager.isAllGranted()) {
            navigator.showMain()
            this.finish()
        }

        window.statusBarColor = resolveThemeColor(R.attr.tintPrimary)

        setContentView(R.layout.permission_activity)

        allow.setOnClickListener {
            requirePermissions(permissions)
        }
    }

    private fun requirePermissions(permissions: Array<String>) {
        if (permissionManager.isAllGranted()) {
            onPermissionSuccess()
        }
        else {
            ActivityCompat.requestPermissions(
                this,
                permissionManager.ungrantedPermissions(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != PERMISSION_REQUEST_CODE) return
        if (permissions.isEmpty()) return
        if (grantResults.isEmpty()) return

        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            Timber.i("All permission secured :)")

            onPermissionSuccess()
        }
        else {
            Timber.i("Request failed :(")

            onPermissionFail()
        }
    }

    private fun onPermissionSuccess() {
        navigator.showMain()
        this.finish()
    }

    private fun onPermissionFail() {
        Popup(this)
            .withTitle(getString(R.string.title_without_permissions))
            .withMessage(getString(R.string.dialog_please_permission))
            .withPositiveButton(R.string.button_ok)
            .show()
    }

    companion object {
        fun callingIntent(context: Context) = Intent(context, GiveMePermissionActivity::class.java)

        private const val PERMISSION_REQUEST_CODE = 99
    }
}