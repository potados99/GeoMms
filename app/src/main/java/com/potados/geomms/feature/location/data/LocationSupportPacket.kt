package com.potados.geomms.feature.location.data

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.potados.geomms.feature.location.LocationSupportProtocol

/**
 * LocationSupportServiceImpl 시스템의 정보 전달 단위입니다.
 * 아래 속성들의 상세 용도는 com.potados.geomms.feature.protocol.LocationSupportPacket을 참고하세요.
 */
data class LocationSupportPacket(
    val type: Int,
    val connectionId: Int,

    val lifeSpan: Long,
    val latitude: Double,
    val longitude: Double
) {
    lateinit var parentConnection: LocationSupportConnection

    companion object {
        private const val DEFAULT_INT = 0
        private const val DEFAULT_LONG = 0L
        private const val DEFAULT_DOUBLE = 0.0

        fun ofCreatingNewRequest(request: LocationSupportRequest) =
            LocationSupportPacket(
                PacketType.REQUEST_CONNECT.number,
                request.id,
                request.lifeSpan,
                DEFAULT_DOUBLE,
                DEFAULT_DOUBLE
            )

        fun ofAcceptingRequest(request: LocationSupportRequest) =
            LocationSupportPacket(
                Companion.PacketType.ACCEPT_CONNECT.number,
                request.id,
                DEFAULT_LONG,
                DEFAULT_DOUBLE,
                DEFAULT_DOUBLE
            )

        fun ofRequestingUpdate(connection: LocationSupportConnection) =
            LocationSupportPacket(
                Companion.PacketType.REQUEST_DATA.number,
                connection.id,
                DEFAULT_LONG,
                DEFAULT_DOUBLE,
                DEFAULT_DOUBLE
            )

        fun ofSendingData(connection: LocationSupportConnection, location: Location) =
            LocationSupportPacket(
                Companion.PacketType.DATA.number,
                connection.id,
                DEFAULT_LONG,
                location.latitude,
                location.longitude
            )

        /**
         * 메시지 타입입니다.
         */
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
                Field.SPAN
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
             * 데이터 보내는 패킷.
             */
            DATA(3, arrayOf(
                Field.TYPE,
                Field.ID,
                Field.LATITUDE,
                Field.LONGITUDE
            )
            ),

            /**
             * 데이터 지금 당장 보내라고 보채는 패킷.
             */
            REQUEST_DATA(4, arrayOf(
                Field.TYPE,
                Field.ID
            )),

            /**
             * 연결 종료를 요청하는 패킷.
             */
            REQUEST_DISCONNECT(5, arrayOf(
                Field.TYPE,
                Field.ID
            ))
        }

        /**
         * 패킷에서 사용하는 필드입니다.
         */
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
            SPAN(
                2,
                "lifeSpan",
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
}