package com.potados.geomms.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class PermissionManagerImpl(
    private val context: Context,
    val permissions: Array<String>
) : PermissionManager {

    private val _isDefaultSms = MutableLiveData<Boolean>()

    init {
        refresh()
    }

    override fun refresh() {
        _isDefaultSms.value = (Telephony.Sms.getDefaultSmsPackage(context) == context.packageName)
    }

    override fun isAllGranted(): Boolean =
        permissions.all(::isGranted)

    override fun ungrantedPermissions(): Array<String> =
        permissions
            .filter { !isGranted(it) }
            .toTypedArray()

    override fun isDefaultSms(): LiveData<Boolean> {
        return _isDefaultSms
    }

    override fun hasReadSms(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    }

    override fun hasSendSms(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    }

    override fun hasContacts(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    override fun hasPhone(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    }

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

}