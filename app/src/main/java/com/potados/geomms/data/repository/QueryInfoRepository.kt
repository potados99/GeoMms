package com.potados.geomms.data.repository

import android.net.Uri
import com.potados.geomms.util.QueryHelper

/**
 * ContentResolver의 query에 사용할 uri 저장소입니다.
 */
interface QueryInfoRepository {
    /**
     * 대화방 목록을 가져올 때에 사용할 uri입니다.
     */
    fun getConversationsUri(): Uri

    /**
     * 특정 thread_id를 지니는 대화방의 모든 메시지를 가져올 때에 사용할 uri입니다.
     */
    fun getMessagesUriOfThread(threadId: Long): Uri

    /**
     * 대화방 목록인 threads 테이블 중 사용할 column만 모아놓았습니다.
     */
    fun getThreadsColumns(): Array<String>

    /**
     * 단문 메시지인 sms 테이블 중 사용할 columns만 모아놓았습니다.
     */
    fun getSmsColumns(): Array<String>

    /**
     * 대화방 목록을 가져올 때에 where절로 Selection 객체입니다.
     * util.QueryHelper.Selection 참고하세요!
     *
     * 특정 대화방만 가져오기 위해서 id 조건을 넘겨줄 수 있습니다.
     */
    fun getConversationsQuerySelection(): QueryHelper.Selection
    fun getConversationsQuerySelection(id: Long): QueryHelper.Selection

    /**
     * 대화방 목록 가져올 때에 사용할 정렬입니다.
     */
    fun getConversationsQueryOrder(): String

    /**
     * 메시지 목록 가져올 때에 사용할 Selection입니다.
     */
    fun getMessagesQuerySelection(): QueryHelper.Selection

    /**
     * 메시지 목록 가져올 때에 사용할 정렬입니다.
     */
    fun getMessageQueryOrder(): String
}