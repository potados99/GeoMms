package com.potados.geomms.feature.message.data

import android.provider.Telephony
import com.google.gson.annotations.SerializedName
import com.potados.geomms.feature.common.ContactRepository
import java.io.Serializable

/**
 * 하나의 대화방에 대한 정보를 가집니다.
 * MessageRepository를 통해 대화방의 메시지를 가져올 수 있습니다.
 */
data class SmsThread(
    @SerializedName(Telephony.ThreadsColumns._ID)               val id: Long,
    @SerializedName(Telephony.ThreadsColumns.RECIPIENT_IDS)     val recipientIds: String,
    @SerializedName(Telephony.ThreadsColumns.DATE)              val date: Long,

    @SerializedName(Telephony.ThreadsColumns.SNIPPET)           val snippet: String,
    @SerializedName(Telephony.ThreadsColumns.MESSAGE_COUNT)     val messageCount: Long,
    @SerializedName(Telephony.ThreadsColumns.READ)              val read: Long,
    @SerializedName(Telephony.ThreadsColumns.TYPE)              val type: Long
) : Serializable {

    /**
     * 전부 읽었는지 여부를 확인합니다.
     */
    fun isAllRead(): Boolean = read == READ_TRUE

    /**
     * 하나라도 안읽었는지 여부를 확인합니다.
     */
    fun isNotAllRead(): Boolean = read == READ_FALSE

    /**
     * 대화 상대방의 canonical id를 모두 가져옵니다. 여러개일 수 있습니다.
     */
    fun recipientIds(): List<Long> = recipientIds.split(' ').map { it.toLong() }

    /**
     * 대화 상대방들의 주소를 가져옵니다.
     */
    fun recipientAddresses(contactRepo: ContactRepository) = recipientIds().map {
        contactRepo.getPhoneNumberByRecipientId(it) ?: throw NullPointerException()
    }

    /**
     * 대화 상대방들의 이름을 가져옵니다.
     */
    fun recipientNames(contactRepo: ContactRepository) = recipientIds().map {
        contactRepo.getContactNameByRecipientId(it) ?: contactRepo.getPhoneNumberByRecipientId(it)
    }

    companion object {
        private const val READ_TRUE = 1L
        private const val READ_FALSE = 0L
    }
}