package com.potados.geomms.manager

import androidx.lifecycle.LiveData

interface PermissionManager {

    fun refresh()

    fun isAllGranted(): Boolean

    fun ungrantedPermissions(): Array<String>

    fun isDefaultSms(): LiveData<Boolean>

    fun hasReadSms(): Boolean

    fun hasSendSms(): Boolean

    fun hasContacts(): Boolean

    fun hasPhone(): Boolean
}