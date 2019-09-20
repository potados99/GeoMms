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

package com.potados.geomms.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber

class PermissionManagerImpl(
    private val context: Context,
    val permissions: Array<String>
) : PermissionManager() {

    private val _isDefaultSms = MutableLiveData<Boolean>()

    init {
        refresh()
    }

    override fun refresh() {
        _isDefaultSms.postValue(isDefaultSms())

        Timber.i("value posted to _isDefaultSms")
    }

    override fun isAllGranted(): Boolean =
        permissions.all(::isGranted)

    override fun ungrantedPermissions(): Array<String> =
        permissions
            .filter { !isGranted(it) }
            .toTypedArray()

    override fun isDefaultSms(): Boolean {
        return (Telephony.Sms.getDefaultSmsPackage(context) == context.packageName)
    }

    override fun isDefaultSmsLiveData(): LiveData<Boolean> {
        return _isDefaultSms
    }

    override fun hasReadSms(): Boolean = isGranted(Manifest.permission.READ_SMS)
    override fun hasSendSms(): Boolean = isGranted(Manifest.permission.SEND_SMS)
    override fun hasContacts(): Boolean = isGranted(Manifest.permission.READ_CONTACTS)
    override fun hasPhone(): Boolean = isGranted(Manifest.permission.READ_PHONE_STATE)
    override fun hasLocation(): Boolean = isGranted(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

}