package com.potados.geomms.feature.message.domain

import com.potados.geomms.util.DateTime
import com.potados.geomms.feature.common.Person
import java.io.Serializable

data class Conversation(
    val id: Long,
    val recipients: List<Person>,
    val date: DateTime,
    val snippet: String,
    val messageCount: Long,
    val allRead: Boolean
) : Serializable {
}