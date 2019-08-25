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
        _isDefaultSms.value = isDefaultSms()

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