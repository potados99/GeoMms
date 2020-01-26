/*
 * Copyright (C) 2019-2020 Song Byeong Jun <potados99@gmail.com>
 *
 * This file is part of GeoMms.
 *
 * GeoMms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoMms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoMms.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.potados.geomms.model

import android.location.Location
import com.potados.geomms.domain.BuildConfig

class Packet(
    var type: Int = 0,
    var connectionId: Long = 0,
    var duration: Long = 0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,

    var address: String = "",
    var date: Long = 0,         // Date received.
    var dateSent: Long = 0,     // Date sent.

    var isInbound: Boolean = false,
    var postFix: String = ""
) {

    companion object {
        fun ofRequestingNewConnection(request: ConnectionRequest, explanation: String = "") =
            Packet(
                type = PacketType.REQUEST_CONNECT.number,
                connectionId = request.connectionId,
                duration = request.duration,
                postFix = if (explanation.isNotEmpty()) "\n$explanation\n${BuildConfig.STORE_LINK}" else ""
            )

        fun ofAcceptingRequest(request: ConnectionRequest) =
            Packet(
                type = PacketType.ACCEPT_CONNECT.number,
                connectionId = request.connectionId
            )

        fun ofRefusingRequest(request: ConnectionRequest) =
            Packet(
                type = PacketType.REFUSE_CONNECT.number,
                connectionId = request.connectionId
            )

        fun ofCancelingRequest(request: ConnectionRequest) =
            Packet(
                type = PacketType.CANCEL_CONNECT.number,
                connectionId = request.connectionId
            )

        fun ofRequestingUpdate(connection: Connection) =
            Packet(
                type = PacketType.REQUEST_DATA.number,
                connectionId = connection.id
            )

        fun ofRequestingDisconnect(connection: Connection) =
            Packet(
                type = PacketType.REQUEST_DISCONNECT.number,
                connectionId = connection.id
            )

        fun ofSendingData(connection: Connection, location: Location) =
            Packet(
                type = PacketType.TRANSFER_DATA.number,
                connectionId = connection.id,
                latitude = location.latitude,
                longitude = location.longitude
            )
    }

    /**
     * NUM      NAME                    DESCRIPTION
     * 1        REQUEST_CONNECT         연결 요청
     * 2        ACCEPT_CONNECT          연결 요청 수락
     * 3        REFUSE_CONNECT          연결 요청 거절
     * 4        CANCEL_CONNECT          연결 요청 취소
     * 5        TRANSFER_DATA           데이터 보내기/받기
     * 6        REQUEST_DATA            데이터 요청
     * 7        REQUEST_DISCONNECT      연결 해제 요청
     */
    enum class PacketType(val number: Int, val fields: Array<Field>) {

        REQUEST_CONNECT(1,
            arrayOf(
                Field.TYPE,
                Field.ID,
                Field.DURATION
            )
        ),
        ACCEPT_CONNECT(2,
            arrayOf(
                Field.TYPE,
                Field.ID
            )
        ),
        REFUSE_CONNECT(3,
            arrayOf(
                Field.TYPE,
                Field.ID
            )
        ),
        CANCEL_CONNECT(4,
            arrayOf(
                Field.TYPE,
                Field.ID
            )
        ),
        TRANSFER_DATA(5,
            arrayOf(
                Field.TYPE,
                Field.ID,
                Field.LATITUDE,
                Field.LONGITUDE
            )
        ),
        REQUEST_DATA(6,
            arrayOf(
                Field.TYPE,
                Field.ID
            )
        ),
        REQUEST_DISCONNECT(7,
            arrayOf(
                Field.TYPE,
                Field.ID
            )
        )
    }

    /**
     * INDEX    NAME                    DESCRIPTION
     * 0        TYPE                    패킷의 종류
     * 1        ID                      연결 식별
     * 2        DURATION                연결의 지속 시간
     * 2        LATITUDE                위도
     * 3        LONGITUDE               경도
     */
    enum class Field(val positionInPayload: Int, val fieldName: String, val convert: String.() -> Number) {

        /** Fixed */
        TYPE(
            0,
            "type",
            { toInt() }
        ),
        ID(
            1,
            "connectionId",
            { toInt() }
        ),

        /** Optional */
        DURATION(
            2,
            "duration",
            { toLong() }
        ),
        LATITUDE(
            2,
            "latitude",
            { toDouble() }
        ),
        LONGITUDE(
            3,
            "longitude",
            { toDouble() }
        ),
    }
}

