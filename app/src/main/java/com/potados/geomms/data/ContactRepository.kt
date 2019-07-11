package com.potados.geomms.data

import android.content.ContentResolver

/**
 * 연락처 접근을 용이하게 해주는 저장소입니다.
 */
interface ContactRepository {
    fun getContactNameByRecipientId(recipientId: Long): String?
    fun getPhoneNumberByRecipientId(recipientId: Long): String?
    fun getContactNameByPhoneNumber(phoneNumber: String): String?
}