package com.potados.geomms.feature.message.data

import android.content.ContentUris
import android.net.Uri
import android.provider.Telephony
import com.potados.geomms.core.util.QueryHelper

class QueryInfoRepositoryImpl : QueryInfoRepository {

    override fun getConversationsUri(): Uri {
        /**
         * "?simple=true"가 따라붙어야 Telephony.ThreadsColumns에 존재하는 column들과 매치됩니다.
         */
        return Telephony.MmsSms.CONTENT_CONVERSATIONS_URI
            .buildUpon()
            .appendQueryParameter("simple", "true")
            .build()
    }

    override fun getMessagesUriOfThreadId(threadId: Long): Uri {
        return ContentUris.withAppendedId(getConversationsUri(), threadId)
    }

    override fun getMessageUriOfMessageId(messageId: Long): Uri {
        return ContentUris.withAppendedId(Telephony.Sms.CONTENT_URI, messageId)
    }

    override fun getThreadsColumns(): Array<String> {
        return arrayOf(
            Telephony.ThreadsColumns._ID,
            Telephony.ThreadsColumns.RECIPIENT_IDS,
            Telephony.ThreadsColumns.SNIPPET,
            Telephony.ThreadsColumns.MESSAGE_COUNT,

            Telephony.ThreadsColumns.DATE,
            Telephony.ThreadsColumns.READ,
            Telephony.ThreadsColumns.TYPE,

            Telephony.ThreadsColumns.ARCHIVED,
            Telephony.ThreadsColumns.HAS_ATTACHMENT,
            Telephony.ThreadsColumns.ERROR,
            Telephony.ThreadsColumns.SNIPPET_CHARSET
        )
    }

    override fun getSmsColumns(): Array<String> {
        return arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,

            Telephony.Sms.DATE,
            Telephony.Sms.READ,
            Telephony.Sms.STATUS,
            Telephony.Sms.TYPE,

            Telephony.Sms.SUBJECT,
            Telephony.Sms.BODY
        )
    }

    override fun getConversationsQuerySelection(): QueryHelper.Selection {
        return QueryHelper()
            .Selection()
            .of(Telephony.Threads.SNIPPET, "<> ''")
    }

    override fun getConversationsQuerySelection(id: Long): QueryHelper.Selection {
        return QueryHelper()
            .Selection()
            .of(Telephony.ThreadsColumns._ID, "=", id)
    }

    override fun getConversationsQueryOrder(): String {
        return "date DESC"
    }

    override fun getMessagesQuerySelection(): QueryHelper.Selection {
        return QueryHelper()
            .Selection()
            .of(Telephony.Sms.BODY, "<> ''")
    }

    override fun getUnreadMessagesQuerySelection(): QueryHelper.Selection {
        return QueryHelper()
            .Selection()
            .of(Telephony.Sms.READ, "=", 0)
    }

    override fun getMessageQueryOrder(): String {
        return "date ASC"
    }

}