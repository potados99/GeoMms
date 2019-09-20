/*
 * Copyright (C) 2019 Song Byeong Jun <potados99@gmail.com>
 *
 * This file is part of GeoMms.
 *
 * GeoMms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoMms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoMms.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.potados.geomms.model

import android.content.ContentUris
import android.net.Uri
import android.provider.Telephony.*
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class Message : RealmObject() {

    enum class AttachmentType { TEXT, IMAGE, VIDEO, AUDIO, SLIDESHOW, NOT_LOADED }

    @PrimaryKey
    var id: Long = 0

    @Index
    var threadId: Long = 0

    /**
     * Content provider가 반환하는 message의 id는 중복될 수 있습니다.
     * MMS와 SMS가 다른 테이블을 사용하기 때문입니다.
     * 이는 Realm object의 primary key로 적절하지 않습니다.
     * 원본 메시지가 필요한 경우를 대비해서 원본 메시지의 id를 저장해줍니다.
     */
    var contentId: Long = 0

    var address: String = ""
    var boxId: Int = 0
    var type: String = ""
    var date: Long = 0
    var dateSent: Long = 0
    var seen: Boolean = false
    var read: Boolean = false
    var locked: Boolean = false
    var subId: Int = -1

    /**
     * SMS
     */
    var body: String = ""
    var errorCode: Int = 0
    var deliveryStatus: Int = Sms.STATUS_NONE

    /**
     * MMS
     */
    var attachmentTypeString: String = AttachmentType.NOT_LOADED.toString()
    var attachmentType: AttachmentType
        get() = AttachmentType.valueOf(attachmentTypeString)
        set(value) {
            attachmentTypeString = value.toString()
        }

    var mmsDeliveryStatusString: String = ""
    var readReportString: String = ""
    var errorType: Int = 0
    var messageSize: Int = 0
    var messageType: Int = 0
    var mmsStatus: Int = 0
    var subject: String = ""
    var textContentType: String = ""
    var parts: RealmList<MmsPart> = RealmList()

    fun getUri(): Uri {
        val baseUri = if (isMms()) Mms.CONTENT_URI else Sms.CONTENT_URI
        return ContentUris.withAppendedId(baseUri, contentId)
    }

    fun isMms(): Boolean = type == "mms"

    fun isSms(): Boolean = type == "sms"

    /**
     * Is this message sent(or will be sent) from me? (right side)
     */
    fun isMe(): Boolean {
        val isIncomingMms = isMms() && (boxId == Mms.MESSAGE_BOX_INBOX || boxId == Mms.MESSAGE_BOX_ALL)
        val isIncomingSms = isSms() && (boxId == Sms.MESSAGE_TYPE_INBOX || boxId == Sms.MESSAGE_TYPE_ALL)

        return !(isIncomingMms || isIncomingSms)
    }

    fun isOutgoingMessage(): Boolean {
        val isOutgoingMms = isMms() && (boxId == Mms.MESSAGE_BOX_OUTBOX)
        val isOutgoingSms = isSms() && (boxId == Sms.MESSAGE_TYPE_FAILED
                || boxId == Sms.MESSAGE_TYPE_OUTBOX
                || boxId == Sms.MESSAGE_TYPE_QUEUED)

        return isOutgoingMms || isOutgoingSms
    }

    /**
     * 클립보드로 복사되어야 할 텍스트
     */
    fun getText(): String {
        return when {
            isSms() -> body
            isMms() -> parts
                .mapNotNull { it.text }
                .joinToString("\n") { text -> text }

            else -> throw IllegalAccessError("$TAG:getText:neither Sms nor Mms.")
        }
    }

    /**
     * 미리보기에 보일 요약 텍스트
     */
    fun getSummary(): String {
        return when {
            isSms() -> body
            isMms() -> StringBuilder().apply {
                getCleansedSubject()
                    .takeIf { it.isNotEmpty() }
                    .let(::appendln)
            }.toString().trim(' ', ',')

            else -> throw IllegalAccessError("$TAG:getSummary:neither Sms nor Mms.")
        }
    }

    fun getCleansedSubject(): String {
        val uselessSubjects = listOf("제목 없음", "no subject")
        return if (uselessSubjects.contains(subject)) "" else subject
    }

    fun isSending(): Boolean {
        return !isFailedMessage() && isOutgoingMessage()
    }

    fun isDelivered(): Boolean {
        val isDeliveredMms = false // TODO
        val isDeliveredSms = deliveryStatus == Sms.STATUS_COMPLETE
        return isDeliveredMms || isDeliveredSms
    }

    fun isFailedMessage(): Boolean {
        val isFailedMms = isMms() && (errorType >= MmsSms.ERR_TYPE_GENERIC_PERMANENT || boxId == Mms.MESSAGE_BOX_FAILED)
        val isFailedSms = isSms() && boxId == Sms.MESSAGE_TYPE_FAILED
        return isFailedMms || isFailedSms
    }

    fun compareSender(other: Message): Boolean = when {
        isMe() && other.isMe() -> subId == other.subId
        !isMe() && !other.isMe() -> subId == other.subId && address == other.address
        else -> false
    }

    companion object {
        private const val TAG = "Message"
    }
}