package com.potados.geomms.data

import android.provider.Telephony
import com.google.gson.annotations.SerializedName

/**
 * 하나의 대화방에 대한 정보를 가집니다.
 * SmsThread는 대화방을 식별하기 위한 데이터 클래스로, 그 자체에는 메시지를 가져오기 위한 기능이 없습니다.
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
) {

    fun isAllRead(): Boolean = read == READ_TRUE
    fun isNotAllRead(): Boolean = read == READ_FALSE

    fun getRecipientIds(): List<Long> = recipientIds.split(' ').map { it.toLong() }

    companion object {
        private const val READ_TRUE = 1L
        private const val READ_FALSE = 0L
    }
}