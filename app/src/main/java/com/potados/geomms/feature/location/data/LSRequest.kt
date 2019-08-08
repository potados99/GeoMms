package com.potados.geomms.feature.location.data

import com.potados.geomms.util.DateTime
import com.potados.geomms.feature.common.Person
import org.koin.core.KoinComponent
import kotlin.random.Random

/**
 * 연결을 위한 하나의 요청을 나타냄.
 */
data class LSRequest(
    val id: Int,
    val person: Person,
    val lifeSpan: Long,
    val isOutBound: Boolean
) {

    class Builder : KoinComponent {
        private var id: Int = Random(System.currentTimeMillis()).nextInt(1, 99999)
        private var person: Person? = null
        private var lifeSpan: Long = 1800000 /* 30분이 기본 */
        private var isOutBound: Boolean = true

        fun setId(id: Int) =
            this.apply { this.id = id }

        fun setPerson(person: Person) =
            this.apply { this.person = person }

        fun setPerson(address: String) =
            this.apply {
                this.person = Person(address)
            }

        fun setLifeSpan(lifeSpan: Long) =
            this.apply { this.lifeSpan = lifeSpan }

        fun setLifeSpan(dateTime: DateTime) =
            this.apply { this.lifeSpan = dateTime.timeStamp }

        fun setAsInBound() =
            this.apply { this.isOutBound = false }

        fun setAsOutBound() =
            this.apply { this.isOutBound = true }

        fun build() =
            LSRequest(
                id,
                person ?: throw IllegalStateException(
                    "Cannot build LSRequest without setting recipient."
                ),
                lifeSpan,
                isOutBound
            )
    }

    companion object {
        fun builder() = Builder()

        fun fromIncomingRequestPacket(address: String, packet: LSPacket) =
            builder()
                .setAsInBound()
                .setId(packet.connectionId)
                .setPerson(address)
                .setLifeSpan(packet.lifeSpan)
                .build()
    }
}