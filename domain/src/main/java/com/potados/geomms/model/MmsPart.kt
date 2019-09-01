package com.potados.geomms.model

import androidx.core.net.toUri
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey

open class MmsPart : RealmObject() {

    @PrimaryKey
    var id: Long = 0
    var type: String = ""
    var text: String? = null

    @LinkingObjects("parts") /* Message::parts */
    val messages: RealmResults<Message>? = null

    fun getUri() = "content://mms/part/$id".toUri()

    fun getSummary(): String? = when {
        type == "text/plain" -> text
        type == "text/x-vCard" -> "Contact card"
        type.startsWith("image") -> "Photo"
        type.startsWith("video") -> "Video"
        else -> null
    }}