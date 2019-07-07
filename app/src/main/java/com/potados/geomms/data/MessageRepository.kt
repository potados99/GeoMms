package com.potados.geomms.data

/**
 * 메시지와 관련된 데이터를 공급해주는 저장소.
 * ContentResolver를 가지고 있는 유일한 repository입니다.
 *
 * Reactive한 것들은 각 액티비티 또는 프래그먼트의 뷰모델에 맡깁니다.
 */
interface MessageRepository {
    fun getSmsThreads(): List<SmsThread>

    fun getSmsThreadById(id: Long): SmsThread

    fun getMessagesFromSmsThread(thread: SmsThread): List<ShortMessage>
}