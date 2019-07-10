package com.potados.geomms.data

/**
 * 메시지와 관련된 데이터를 공급해주는 저장소.
 * ContentResolver를 가지고 있는 유일한 repository입니다.
 *
 * Reactive한 것들은 각 액티비티 또는 프래그먼트의 뷰모델에 맡깁니다.
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