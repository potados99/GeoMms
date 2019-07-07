package com.potados.geomms.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.Telephony

class QueryInfoRepositoryImpl : QueryInfoRepository {

    /**
     * 모든 대화방의 목록을 가져오고 싶을 때에 사용할 uri입니다.
     * "?simple=true"가 따라붙어야 Telephony.ThreadsColumns에 존재하는 column들과 매치됩니다.
     */
    override fun getConversationsUri(): Uri {
        return Telephony.Threads.CONTENT_URI
            .buildUpon()
            .appendQueryParameter("simple", "true")
            .build()
    }

    /**
     * 특정 대화 스레드의 모든 메시지를 가져오고 싶을 때에 사용할 uri입니다.
     */
    override fun getMessagesUriOfThread(threadId: Long): Uri {
        return ContentUris.withAppendedId(getConversationsUri(), threadId)
    }

    /**
     * mms-sms 데이터베이스의 threads 테이블에 존재하는 column 중 일부입니다.
     */
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

    /**
     * sms 테이블에 존재하는 column 중 일부입니다.
     */
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


    /**
     * 대화 리스트 쿼리할 때에 쓸 WHERE 조건입니다.
     */
    override fun getConversationsQuerySelection(): String {
        return ""
    }

    /**
     * 대화 리스트 중 특정 id만 가져올 때에 쓸 WHERE 조건입니다.
     */
    override fun getConversationsQuerySelection(id: Long): String {
        return "${Telephony.ThreadsColumns._ID} = $id"
    }

    /**
     * 특정 대화에 속하는 메시지들 쿼리할 때에 쓸 WHERE 조건입니다.
     */
    override fun getMessagesQuerySelection(): String {
        return "body <> ''"
    }

    /**
     * 대화 리스트 쿼리할 때에 쓸 ORDER 조건입니다.
     */
    override fun getConversationsQueryOrder(): String {
        return "date DESC"
    }

    /**
     * 특정 대화에 속하는 메시지들 쿼리할 때에 쓸 ORDER 조건입니다.
     */
    override fun getMessageQueryOrder(): String {
        return "date ASC"
    }

}