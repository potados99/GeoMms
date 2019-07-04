package com.potados.geomms.data

import com.google.gson.annotations.SerializedName

data class ShortMessage(
    @SerializedName(COL_ID)             val id: Long,
    @SerializedName(COL_THREAD_ID)      val threadId: Long,
    @SerializedName(COL_ADDRESS)        val address: String,
    @SerializedName(COL_PERSON)         val person: Long,

    @SerializedName(COL_DATE)           val date: Long,
    @SerializedName(COL_READ)           val read: Long,
    @SerializedName(COL_STATUS)         val status: Long,
    @SerializedName(COL_TYPE)           val type: Long,

    @SerializedName(COL_SUBJECT)        val subject: String,
    @SerializedName(COL_BODY)           val body: String
) {

    companion object {
        const val TYPE_RECEIVED = 1
        const val TYPE_SENT = 2

        const val COL_ID = "_id"
        const val COL_THREAD_ID = "thread_id"
        const val COL_ADDRESS = "address"
        const val COL_PERSON = "person"

        const val COL_DATE = "date"
        const val COL_READ = "read"
        const val COL_STATUS = "status"
        const val COL_TYPE = "type"

        const val COL_SUBJECT = "subject"
        const val COL_BODY = "body"
    }
}