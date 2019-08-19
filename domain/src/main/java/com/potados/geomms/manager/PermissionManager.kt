package com.potados.geomms.manager

import androidx.lifecycle.LiveData
import io.reactivex.Flowable

interface PermissionManager {

    fun refresh()

    fun isAllGranted(): Boolean

    fun ungrantedPermissions(): Array<String>

    fun isDefaultSms(): Boolean

    fun isDefaultSmsLiveData(): LiveData<Boolean>

    fun hasReadSms(): Boolean

    fun hasSendSms(): Boolean

    fun hasContacts(): Boolean

    fun hasPhone(): Boolean
}