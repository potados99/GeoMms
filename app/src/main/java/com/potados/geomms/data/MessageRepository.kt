package com.potados.geomms.data

/**
 * 메시지와 관련된 데이터를 공급해주는 저장소.
 * ContentResolver를 가지고 있는 유일한 repository입니다.
 *
 * Repository 계층에서의 caching은 없습니다. 호출할 때마다 resolver에 query를 날립니다.
 * 하지만 resolver가 caching을 해줍니다. (ContentResolver.query 문서)
 */
interface MessageRepository {

    /**
     * 기기에 존재하는 모든 대화방 정보를 불러옵니다.
     */
    fun getSmsThreads(): List<SmsThread>

    /**
     * 대화방 id를 이용해 특정 대화방을 찾아냅니다.
     */
    fun getSmsThreadById(id: Long): SmsThread

    /**
     * 특정 대화방에 속하는 모든 메시지를 가져옵니다. (sms 한정)
     */
    fun getMessagesFromSmsThread(thread: SmsThread): List<ShortMessage>
}