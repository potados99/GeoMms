package com.potados.geomms.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import com.potados.geomms.util.QueryHelper

class QueryInfoRepositoryImpl : QueryInfoRepository {

    override fun getConversationsUri(): Uri {
        /**
         * "?simple=true"가 따라붙어야 Telephony.ThreadsColumns에 존재하는 column들과 매치됩니다.
         */
        return Telephony.Threads.CONTENT_URI
            .buildUpon()
            .appendQueryParameter("simple", "true")
            .build()
    }

    override fun getMessagesUriOfThread(threadId: Long): Uri {
        return ContentUris.withAppendedId(getConversationsUri(), threadId)
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
        return QueryHelper().Selection()
    }

    override fun getConversationsQuerySelection(id: Long): QueryHelper.Selection {
        return QueryHelper()
            .Selection()
            .of(Telephony.ThreadsColumns._ID, "=", id)
    }

    override fun getMessagesQuerySelection(): QueryHelper.Selection {
        val a = QueryHelper()
            .Selection()
            .of(Telephony.Sms.BODY, "<> ''")

        Log.d("HEYYyyyyyyyy", a.getSelection())
        a.getSelectionArgs()?.forEach {
            Log.d("Hhhhhhhgh", it)
        }

        return a
    }

    override fun getConversationsQueryOrder(): String {
        return "date DESC"
    }

    override fun getMessageQueryOrder(): String {
        return "date ASC"
    }

}