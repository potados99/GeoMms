package com.potados.geomms.feature.location.data

import com.potados.geomms.core.util.DateTime
import com.potados.geomms.feature.common.ContactRepository
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.random.Random

/**
 * 연결을 위한 하나의 요청을 나타냄.
 */
data class LocationSupportRequest(
    val id: Int,
    val person: LocationSupportPerson,
    val lifeSpan: Long,
    val isOutBound: Boolean
) {

    class Builder : KoinComponent {
        private var id: Int = Random(System.currentTimeMillis()).nextInt(1, 99999)
        private var person: LocationSupportPerson? = null
        private var lifeSpan: Long = 1800000 /* 30분이 기본 */
        private var isOutBound: Boolean = true

        private val contactRepository: ContactRepository by inject()

        fun setId(id: Int) =
            this.apply { this.id = id }

        fun setPerson(person: LocationSupportPerson) =
            this.apply { this.person = person }

        fun setPerson(address: String) =
            this.apply {
                this.person = LocationSupportPerson(
                    address,
                    contactRepository.getContactNameByPhoneNumber(address) ?: address
                    )
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
            LocationSupportRequest(
                id,
                person ?: throw IllegalStateException(
                    "Cannot build LocationSupportRequest without setting person."
                ),
                lifeSpan,
                isOutBound
            )
    }

    companion object {
        fun builder() = Builder()

        fun fromIncomingRequestPacket(address: String, packet: LocationSupportPacket) =
            builder()
                .setAsInBound()
                .setId(packet.connectionId)
                .setPerson(address)
                .setLifeSpan(packet.lifeSpan)
                .build()
    }
}