package com.potados.geomms.data

data class Sms(val id: Long,
               val threadId: Long,
               val type: Long,
               val address: String,
               val date: Long,
               val body: String) {

    companion object {
        const val TYPE_RECEIVED = 1
        const val TYPE_SENT = 2

        const val COL_ID = "_id"
        const val COL_THREAD_ID = "thread_id"
        const val COL_TYPE = "type"
        const val COL_ADDRESS = "address"
        const val COL_DATE = "date"
        const val COL_BODY = "body"
    }
}