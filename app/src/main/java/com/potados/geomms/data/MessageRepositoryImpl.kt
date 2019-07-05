package com.potados.geomms.data

import android.content.ContentResolver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.potados.geomms.util.QueryHelper

class MessageRepositoryImpl(private val resolver: ContentResolver) : MessageRepository {

    private val projection = arrayOf(
        ShortMessage.COL_ID,
        ShortMessage.COL_THREAD_ID,
        ShortMessage.COL_ADDRESS,

        ShortMessage.COL_DATE,
        ShortMessage.COL_READ,
        ShortMessage.COL_STATUS,
        ShortMessage.COL_TYPE,

        ShortMessage.COL_SUBJECT,
        ShortMessage.COL_BODY
    )
    private val conversationsUriString = "content://mms-sms/conversations"
    // private val smsUriString = "content://sms"

    private val conversationHeads = mutableListOf<ShortMessage>()
    private val liveConversationHeads = MutableLiveData<List<ShortMessage>>()

    init {
        updateConversationList()
    }

    override fun updateConversationList() {
        conversationHeads.clear()
        conversationHeads.addAll(
            QueryHelper.queryToCollection<List<ShortMessage>>(resolver, conversationsUriString, projection, "body <> ''", "date DESC")
        )

        liveConversationHeads.value = conversationHeads
    }

    override fun getConversationHeads(): List<ShortMessage> = conversationHeads

    override fun getLiveConversationHeads(): LiveData<List<ShortMessage>> = liveConversationHeads

    override fun getSmsThreadByThreadId(threadId: Long): SmsThread =
        SmsThread(
            QueryHelper.queryToCollection(resolver, "$conversationsUriString/$threadId", projection, "body <> ''", "date ASC")
        )

    override fun addSms(sms: ShortMessage) {

    }

    override fun deleteSms(id: Int) {
    }

}