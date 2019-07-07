package com.potados.geomms.data

import android.net.Uri

/**
 * ContentResolver의 query에 사용할 uri 저장소입니다.
 */
interface QueryInfoRepository {
    fun getConversationsUri(): Uri
    fun getMessagesUriOfThread(threadId: Long): Uri

    fun getThreadsColumns(): Array<String>
    fun getSmsColumns(): Array<String>

    fun getConversationsQuerySelection(): String
    fun getConversationsQuerySelection(id: Long): String

    fun getMessagesQuerySelection(): String

    fun getConversationsQueryOrder(): String
    fun getMessageQueryOrder(): String
}