package com.potados.geomms.manager

interface PermissionManager {

    fun isAllGranted(): Boolean

    fun ungrantedPermissions(): List<String>

    fun isDefaultSms(): Boolean

    fun hasReadSms(): Boolean

    fun hasSendSms(): Boolean

    fun hasContacts(): Boolean

    fun hasPhone(): Boolean
}