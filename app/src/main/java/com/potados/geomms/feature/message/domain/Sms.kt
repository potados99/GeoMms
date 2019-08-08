package com.potados.geomms.feature.message.domain

import com.potados.geomms.util.DateTime
import com.potados.geomms.feature.common.Person

data class Sms(
    val id: Long,
    val recipient: Person,
    val date: DateTime,
    val body: String,
    val isSent: Boolean
    ) {
}