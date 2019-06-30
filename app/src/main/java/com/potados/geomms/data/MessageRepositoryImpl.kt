package com.potados.geomms.data

import android.content.ContentResolver
import android.net.Uri

class MessageRepositoryImpl(private val resolver: ContentResolver) : MessageRepository {

    private val projection = arrayOf(
        Sms.COL_ID,
        Sms.COL_THREAD_ID,
        Sms.COL_TYPE,
        Sms.COL_ADDRESS,
        Sms.COL_DATE,
        Sms.COL_BODY
    )
    private val conversationsUriString = "content://mms-sms/conversations"
    private val smsUriString = "content://sms"

    override fun updateRepository() {
    }

    override fun getConversationHeads(): List<Sms> = querySmsToList(resolver, conversationsUriString)

    override fun getSmsThreadByThreadId(threadId: Int): SmsThread = SmsThread(querySmsToList(resolver, "$conversationsUriString/$threadId"))

    override fun addSms(sms: Sms) {
    }

    override fun deleteSms(id: Int) {
    }


    private fun querySmsToList(resolver: ContentResolver, uriString: String, where: String? = null): List<Sms> =
        mutableListOf<Sms>().apply {
            resolver.query(Uri.parse(uriString), projection, where, null, null)?.apply {
                if (moveToFirst()) {
                    do {
                        add(
                            Sms(
                                id = getLong(0), /* 실패 불가. Integer 타입임. */
                                threadId = getLong(1),
                                type = getLong(2),
                                address = getString(3),
                                date = getLong(4),
                                body = getString(5)
                            )
                        )
                    } while (moveToNext())
                }

                close()
            }
        }

}