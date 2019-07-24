package com.potados.geomms.feature.location.data

import com.potados.geomms.core.util.DateTime
import kotlin.random.Random

/**
 * 연결을 위한 하나의 요청을 나타냄.
 */
data class LocationSupportRequest(
    val id: Long,
    val person: LocationSupportPerson,
    val lifeSpan: Long,
    val isOutBound: Boolean
) {

    class Builder {
        private val id: Long = Random(0).nextLong(1, 99999)
        private var person: LocationSupportPerson? = null
        private var lifeSpan: Long = 1800000 /* 30분이 기본 */
        private var isOutBound: Boolean = true

        fun setPerson(person: LocationSupportPerson) =
            this.apply { this.person = person }

        fun setPerson(displayName: String, address: String) =
            this.apply { this.person = LocationSupportPerson(displayName, address) }

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
    }
}