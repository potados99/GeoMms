package com.potados.geomms.repository

import com.potados.geomms.model.Attachment
import com.potados.geomms.model.Message
import com.potados.geomms.model.MmsPart
import io.realm.RealmResults

abstract class MessageRepository : Repository() {

    abstract fun getMessages(threadId: Long, query: String = ""): RealmResults<Message>?

    abstract fun getMessage(id: Long): Message?

    abstract fun getMessageForPart(id: Long): Message?

    abstract fun getUnreadCount(): Long?

    abstract fun getPart(id: Long): MmsPart?

    abstract fun getPartsForConversation(threadId: Long): RealmResults<MmsPart>?

    /**
     * Retrieves the list of messages which should be shown in the notification
     * for a given conversation
     */
    abstract fun getUnreadUnseenMessages(threadId: Long): RealmResults<Message>?

    /**
     * Retrieves the list of messages which should be shown in the quickreply popup
     * for a given conversation
     */
    abstract fun getUnreadMessages(threadId: Long): RealmResults<Message>?

    abstract fun markAllSeen()

    abstract fun markSeen(threadId: Long)

    abstract fun markRead(vararg threadIds: Long)

    abstract fun markUnread(vararg threadIds: Long)

    abstract fun sendMessage(
        subId: Int,
        threadId: Long,
        addresses: List<String>,
        body: String,
        attachments: List<Attachment>
    )

    /**
     * Attempts to send the SMS message. This can be called if the message has already been persisted
     */
    abstract fun sendSms(message: Message)

    abstract fun insertSentSms(subId: Int, threadId: Long, address: String, body: String, date: Long): Message?

    abstract fun insertReceivedSms(subId: Int, address: String, body: String, sentTime: Long): Message?

    /**
     * Marks the message as sending, in case we need to retry sending it
     */
    abstract fun markSending(id: Long)

    abstract fun markSent(id: Long)

    abstract fun markFailed(id: Long, resultCode: Int)

    abstract fun markDelivered(id: Long)

    abstract fun markDeliveryFailed(id: Long, resultCode: Int)

    abstract fun deleteMessages(vararg messageIds: Long)
}