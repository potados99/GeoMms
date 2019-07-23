package com.potados.geomms.core.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class PermissionChecker(
    private val context: Context,
    private val permissions: Array<String>) {

    private val ungranted = mutableListOf<String>()

    init {
        update()
    }

    fun isAllGranted(): Boolean {
        return ungranted.isEmpty()
    }

    fun ungrantedPermissions(): Array<String> {
        return ungranted.toTypedArray()
    }

    fun update() {
        checkAllPermissions()
    }

    private fun checkAllPermissions() {
        ungranted.clear()

        permissions.forEach {
            val permissionCheckResult = ContextCompat.checkSelfPermission(context, it)
            val isGranted = (permissionCheckResult == PackageManager.PERMISSION_GRANTED)

            if (!isGranted) {
                ungranted.add(it)
            }
        }
    }
}