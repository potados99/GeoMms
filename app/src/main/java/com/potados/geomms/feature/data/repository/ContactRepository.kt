package com.potados.geomms.feature.data.repository

/**
 * 연락처 접근을 용이하게 해주는 저장소입니다.
 */
interface ContactRepository {
    /**
     * recipient id를 이용해 연락처 이름을 알아냅니다.
     */
    fun getContactNameByRecipientId(recipientId: Long): String?

    /**
     * recipient id를 이용해 전화번호를 알아냅니다.
     */
    fun getPhoneNumberByRecipientId(recipientId: Long): String?

    /**
     * 전화번호를 이용해 연락처 이름을 알아냅니다.
     */
    fun getContactNameByPhoneNumber(phoneNumber: String): String?
}