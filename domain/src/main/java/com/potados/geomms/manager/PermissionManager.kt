package com.potados.geomms.manager

import androidx.lifecycle.LiveData

abstract class PermissionManager : Manager() {

    abstract fun refresh()

    abstract fun isAllGranted(): Boolean

    abstract fun ungrantedPermissions(): Array<String>

    abstract fun isDefaultSms(): Boolean

    abstract fun isDefaultSmsLiveData(): LiveData<Boolean>

    abstract fun hasReadSms(): Boolean

    abstract fun hasSendSms(): Boolean

    abstract fun hasContacts(): Boolean

    abstract fun hasPhone(): Boolean

    abstract fun hasLocation(): Boolean
}