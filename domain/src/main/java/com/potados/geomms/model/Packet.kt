package com.potados.geomms.model

import android.location.Location

class Packet(
    var type: Int = 0,
    var connectionId: Long = 0,
    var duration: Long = 0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,

    var address: String = "",
    var date: Long = 0, // 받은 시점
    var dateSent: Long = 0, // 보낸 시점

    var isInbound: Boolean = false

) {

    companion object {

        fun ofRequestingNewConnection(request: ConnectionRequest) =
            Packet(
                type = PacketType.REQUEST_CONNECT.number,
                connectionId = request.connectionId,
                duration = request.duration
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
                type = PacketType.DATA.number,
                connectionId = connection.id,
                latitude = location.latitude,
                longitude = location.longitude
            )
    }

    enum class PacketType(
        val number: Int,
        val fields: Array<Field>
    ) {

        /**
         * 연결 요청 보내는 패킷
         */
        REQUEST_CONNECT(1, arrayOf(
            Field.TYPE,
            Field.ID,
            Field.DURATION
        )
        ),

        /**
         * 연결 요청 수락하는 패킷
         */
        ACCEPT_CONNECT(2, arrayOf(
            Field.TYPE,
            Field.ID
        )
        ),

        /**
         * 연결 요청 거절하는 패킷
         */
        REFUSE_CONNECT(3, arrayOf(
            Field.TYPE,
            Field.ID
        )
        ),

        /**
         * 데이터 보내는 패킷.
         */
        DATA(4, arrayOf(
            Field.TYPE,
            Field.ID,
            Field.LATITUDE,
            Field.LONGITUDE
        )
        ),

        /**
         * 데이터 지금 당장 보내라고 보채는 패킷.
         */
        REQUEST_DATA(5, arrayOf(
            Field.TYPE,
            Field.ID
        )),

        /**
         * 연결 종료를 요청하는 패킷.
         */
        REQUEST_DISCONNECT(6, arrayOf(
            Field.TYPE,
            Field.ID
        ))
    }

    enum class Field(
        val positionInPayload: Int,         /* 직렬화된 페이로드에서 해당 필드의 위치. */
        val fieldName: String,              /* 해당 필드의 이름. */
        val convert: (String) -> Number     /* 해당 필드를 숫자로 바꾸기 위한 람다식. */
    ) {

        /******************************
         * 고정 필드
         ******************************/

        /** 패킷의 종류. */
        TYPE(
            0,
            "type",
            {str -> str.toInt()}
        ),

        /** 연결 connectionId. */
        ID(
            1,
            "connectionId",
            {str -> str.toInt()}
        ),


        /******************************
         * 가변 필드
         ******************************/

        /** 연결 요청할 때에 사용할 연결시간. */
        DURATION(
            2,
            "duration",
            {str -> str.toLong()}
        ),

        /** 위치 데이터 보낼 때에 사용할 경도 필드. */
        LATITUDE(
            2,
            "latitude",
            {str -> str.toDouble()}
        ),

        /** 위치 데이터 보낼 때에 사용할 위도 필드. */
        LONGITUDE(
            3,
            "longitude",
            {str -> str.toDouble()}
        ),
    }
}

