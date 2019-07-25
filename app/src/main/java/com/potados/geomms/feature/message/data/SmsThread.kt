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
    fun getRecipientIds(): List<Long> = recipientIds.split(' ').map { it.toLong() }

    /**
     * 대화 상대들을 사람이 식별할 수 있는 정보로 바꾸어 하나의 문자열로 합칩니다.
     *
     * 안드로이드 threads 테이블의 recipients_id column은 상대방(들)의 연락처를 canonical connectionId 형태로 표현합니다.
     * 이를 전화번호로 변환하여야 하며, (이것은 실패해서는 안됨.)
     * 그 전화번호가 연락처에 존재하는 경우 다시 연락처 이름으로 바꾸어야 합니다.
     *
     * @param contactRepo 그냥은 연락처에 접근할 수 없습니다. ContactRepository를 통해야 합니다.
     */
    fun getRecipientString(contactRepo: ContactRepository): String = StringBuilder().apply {
            getRecipientIds().forEach { id ->

                val phoneNumberString = contactRepo.getPhoneNumberByRecipientId(id) ?: throw IllegalArgumentException("Wrong recipient connectionId: $id")
                val nameString = contactRepo.getContactNameByPhoneNumber(phoneNumberString)

                /*
                 * 이름이 확인될 경우 이를 사용하고, 그렇지 않을 경우 전화번호를 사용합니다.
                 * 각 연락처들 사이에 쉼표를 추가해줍니다.
                 */
                append(nameString ?: phoneNumberString)
                append(", ")
            }

        }.trim(',', ' ').toString()

    companion object {
        private const val READ_TRUE = 1L
        private const val READ_FALSE = 0L
    }
}