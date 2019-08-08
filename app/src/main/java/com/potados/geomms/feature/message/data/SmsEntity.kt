package com.potados.geomms.feature.message.data

import android.provider.Telephony
import com.google.gson.annotations.SerializedName
import com.potados.geomms.util.DateTime
import com.potados.geomms.feature.common.ContactRepository
import com.potados.geomms.feature.common.Person
import com.potados.geomms.feature.message.domain.Sms

/**
 * 하나의 SMS를 나타냅니다.
 */
data class SmsEntity(
    @SerializedName(Telephony.Sms._ID)              val id: Long,
    @SerializedName(Telephony.Sms.THREAD_ID)        val threadId: Long,
    @SerializedName(Telephony.Sms.ADDRESS)          val address: String,

    @SerializedName(Telephony.Sms.DATE)             val date: Long,
    @SerializedName(Telephony.Sms.READ)             val read: Long,
    @SerializedName(Telephony.Sms.STATUS)           val status: Long,
    @SerializedName(Telephony.Sms.TYPE)             val type: Long,

    @SerializedName(Telephony.Sms.SUBJECT)          val subject: String,
    @SerializedName(Telephony.Sms.BODY)             val body: String
) {
    fun isSent(): Boolean = (this.type.toInt() == Telephony.Sms.MESSAGE_TYPE_SENT)
    fun isReceived(): Boolean = (this.type.toInt() == Telephony.Sms.MESSAGE_TYPE_INBOX)

    fun isRead(): Boolean = (this.read == READ_TRUE)
    fun isNotRead(): Boolean = (this.read == READ_FALSE)

    fun toSms(contactRepository: ContactRepository) =
        Sms(
            id = id,
            recipient = Person(address),
            date = DateTime(date),
            body = body,
            isSent = isSent()
        )

    companion object {
        const val READ_TRUE = 1L
        const val READ_FALSE = 0L
    }
}