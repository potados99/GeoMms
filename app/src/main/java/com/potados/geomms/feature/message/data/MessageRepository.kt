package com.potados.geomms.feature.message.data

import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase.None
import com.potados.geomms.feature.common.Person
import com.potados.geomms.feature.message.domain.Conversation
import com.potados.geomms.feature.message.domain.Sms

/**
 * 메시지와 관련된 데이터를 공급해주는 저장소입니다.
 *
 * Repository 계층에서의 caching은 없습니다. 호출할 때마다 resolver에 query를 날립니다.
 * 하지만 resolver가 caching을 해줍니다. (ContentResolver.query 문서)
 */
interface MessageRepository {

    /**
     * 기기에 존재하는 모든 대화방 정보를 불러옵니다.
     */
    fun getConversations(): Result<List<Conversation>>

    /**
     * 대화방을 지웁니다.
     */
    fun removeConversations(conversation: Conversation): Result<None>

    /**
     * 대화방 id를 이용해 특정 대화방을 찾아냅니다.
     */
    fun getConversationById(id: Long): Result<Conversation>

    /**
     * 특정 대화방에 속하는 모든 메시지를 가져옵니다. (sms 한정)
     */
    fun getMessagesInConversation(conversation: Conversation): Result<List<Sms>>

    /**
     * 하나의 SMS를 지웁니다.
     */
    fun removeSms(sms: Sms): Result<None>

    /**
     * 대화방의 모든 메시지를 읽음 처리합니다.
     */
    fun markConversationAsRead(conversation: Conversation): Result<None>
}