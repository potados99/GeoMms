package com.potados.geomms.protocol

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.potados.geomms.data.entity.LocationSupportPacket
import com.potados.geomms.core.util.Reflection
import com.potados.geomms.core.util.Types

/**
 * LocationSupport 프로토콜에 관한 static 메소드를 제공합니다.
 * 패킷 parse하기, 직렬화하기, 패킷 만들기 등...
 */
class LocationSupportProtocol {

    companion object {

        /**
         * 메시지 구성 요소:
         * - 타입
         * - 연결 식별자
         * - 필드...
         */

        private const val DEFAULT_INT = 0
        private const val DEFAULT_LONG = 0L
        private const val DEFAULT_DOUBLE = 0.0

        private const val GEO_MMS_PREFIX = "[GEOMMS]"
        private const val FIELD_SPLITTER = ':'

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
                LocationSupportProtocol.Companion.Field.TYPE,
                LocationSupportProtocol.Companion.Field.ID,
                LocationSupportProtocol.Companion.Field.SPAN
                )
            ),

            /**
             * 연결 요청 수락하는 패킷
             */
            ACCEPT_CONNECT(2, arrayOf(
                LocationSupportProtocol.Companion.Field.TYPE,
                LocationSupportProtocol.Companion.Field.ID
                )
            ),

            /**
             * 데이터 보내는 패킷.
             */
            DATA(3, arrayOf(
                LocationSupportProtocol.Companion.Field.TYPE,
                LocationSupportProtocol.Companion.Field.ID,
                LocationSupportProtocol.Companion.Field.LATITUDE,
                LocationSupportProtocol.Companion.Field.LONGITUDE
                )
            ),

            /**
             * 데이터 지금 당장 보내라고 보채는 패킷.
             */
            REQUEST_DATA(4, arrayOf(
                LocationSupportProtocol.Companion.Field.TYPE,
                LocationSupportProtocol.Companion.Field.ID
            )),

            /**
             * 연결 종료를 요청하는 패킷.
             */
            REQUEST_DISCONNECT(5, arrayOf(
            LocationSupportProtocol.Companion.Field.TYPE,
            LocationSupportProtocol.Companion.Field.ID
            ))
        }

        /**
         * 패킷에서 사용하는 필드입니다.
         */
        private enum class Field(
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

            /** 연결 id. */
            ID(
                1,
                "id",
                {str -> str.toInt()}
            ),


            /******************************
             * 가변 필드
             ******************************/

            /** 연결 요청할 때에 사용할 연결시간. */
            SPAN(
                2,
                "span",
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

        /**
         * 메시지를 읽어서 LocationSupportPacket 객체로 만들어줍니다.
         *
         * @return 접두어가 없거나, 이상하거나, 포맷이 안 맞으면 null을 반환합니다.
         */
        fun parse(body: String): LocationSupportPacket? {
            /**
             * 예외처리
             */
            if (!isLocationSupportMessage(body)) {
                Log.w("LocationSupportProtocol: parseMessage", "not a LocationSupport packet.")
                return null
            }

            /**
             * 페이로드의 필드들 가져오기.
             */
            val payload = body.removePrefix(GEO_MMS_PREFIX)
            val payloadFields = payload.split(FIELD_SPLITTER)

            /**
             * 고정 필드가 type과 id로 두 개인데, 이보다 적으면 잘못된 패킷으로 간주.
             */
            if (payloadFields.size < 2) {
                Log.w("LocationSupportProtocol: parseMessage", "necessary fields are missing.")
                return null
            }

            /**
             * 페이로드 중 고정 부분의 첫번째 필드인 type 숫자 값을 가져옵니다.
             */
            val typeNumber = with(Field.TYPE) {
                convert(payloadFields[positionInPayload])
            }

            /**
             * 해당 typeNumber에 해당하는 PacketType을 가져옵니다.
             */
            val type = findType(typeNumber)

            /**
             * 없으면 잘못된 패킷.
             */
            if (type == null) {
                Log.w("LocationSupportProtocol: parse", "undefined type: $typeNumber")
                return null
            }

            /**
             * json으로 만들어서 담을겁니다.
             */
            val json = JsonObject()

            /**
             * 가져온 타입에 해당하는 필드들을 가지고 와서,
             * {필드 이름}과 {페이로드에서 가져온 그 필드의 값} 쌍을 json 객체에 추가해줍니다.
             */
            type.fields.forEach {
                val value = try {
                    /**
                     * Convert를 굳이 여기서 해주는 이유는, 숫자 스트링이 무결한지 여기에서 확인하기 위함입니다.
                     * Gson이 검사하게 해도 되는데 그냥 직접 하고 싶었습니다.
                     */
                    it.convert(payloadFields[it.positionInPayload])
                }
                catch (e: Exception) {
                    when (e) {
                        /** toDouble이나 toLong에서 문제가 생긴 경우 */
                        is NumberFormatException -> {
                            Log.d("LocationSupportProtocol: parseMessage",
                                "parse error at payload field ${it.positionInPayload}: ${payloadFields[it.positionInPayload]}")
                        }
                        else -> { /* 그렇지 않은 경우 */
                            Log.d("LocationSupportProtocol: parseMessage",
                                "unknown error occurred while adding parsed number to json object.")
                        }
                    }

                    return null
                }

                json.addProperty(it.fieldName, value)
            }

            /**
             * LocationSupportPacket 객체 확보.
             */
            return try {
                Gson().fromJson(json, Types.typeOf<LocationSupportPacket>())
            }
            catch (e: Exception) {
                when (e) {
                    is JsonSyntaxException -> {
                        Log.d("LocationSupportProtocol: parseMessage", "error while parsing json. json syntax incorrect.")
                    }
                    else -> {
                        Log.d("LocationSupportProtocol: parseMessage", "unknown error occurred while parsing json.")
                    }
                }
                return null
            }
        }

        /**
         * 메시지가 LocationSupportManagerImpl 메시지인지 확인합니다.
         * 판단 기준은, 앞에 GEO_MMS_PREFIX 접두어가 붙었는가 입니다.
         */
        fun isLocationSupportMessage(body: String): Boolean {
            if (body.isBlank())                     return false   /* 비어있는 메시지 */
            if (!body.startsWith(GEO_MMS_PREFIX))   return false   /* 무관한 메시지 */

            return true
        }

        /**
         * LocationSupportPacket 객체를 SMS로 보낼 수 있게 직렬화해줍니다.
         * toString()의 기능을 한다고 볼 수 있습니다.
         */
        fun serialize(locationPacket: LocationSupportPacket): String? {

            val builder = StringBuilder().append(GEO_MMS_PREFIX)

            val type = PacketType.values().find { it.number == locationPacket.type }

            if (type == null) {
                Log.d("LocationSupportProtocol: serialize", "wrong packet type: ${locationPacket.type}")
                return null
            }

            type.fields.forEach {
                val value = try {
                    Reflection.readInstanceProperty<Any>(locationPacket, it.fieldName).toString()
                }
                catch (e: Exception) {
                    Log.d("LocationSupportProtocol: serialize", "error occurred while accessing property.")
                    return null
                }

                builder.append(value)

                if (it != type.fields.last()) {
                    builder.append(FIELD_SPLITTER)
                }
            }

            return builder.toString()
        }

        /**
         * 타입 번호를 통해 PacketType 객체를 가져옵니다.
         */
        fun findType(typeNum: Number): PacketType? {
            return PacketType.values().find { it.number == typeNum }
        }

        /**
         * 연결 요청 패킷을 생성합니다.
         */
        fun createRequestConnectPacket(id: Int, span: Long): LocationSupportPacket =
            LocationSupportPacket(PacketType.REQUEST_CONNECT.number, id, span, DEFAULT_DOUBLE, DEFAULT_DOUBLE)

        /**
         * 연결 수락 패킷을 생성합니다.
         */
        fun createAcceptConnectPacket(id: Int): LocationSupportPacket =
            LocationSupportPacket(PacketType.ACCEPT_CONNECT.number, id, DEFAULT_LONG, DEFAULT_DOUBLE, DEFAULT_DOUBLE)

        /**
         * 데이터 패킷을 생성합니다.
         */
        fun createDataPacket(id: Int, lat: Double, long: Double): LocationSupportPacket =
            LocationSupportPacket(PacketType.DATA.number, id, DEFAULT_LONG, lat, long)

        /**
         * 데이터 전송 요청 패킷을 생성합니다.
         */
        fun createRequestDataPacket(id: Int): LocationSupportPacket =
            LocationSupportPacket(PacketType.REQUEST_DATA.number, id, DEFAULT_LONG, DEFAULT_DOUBLE, DEFAULT_DOUBLE)

        /**
         * 연결 종료 요청 패킷을 생성합니다.
         */
        fun createRequestDisconnectPacket(id: Int): LocationSupportPacket =
            LocationSupportPacket(PacketType.REQUEST_DISCONNECT.number, id, DEFAULT_LONG, DEFAULT_DOUBLE, DEFAULT_DOUBLE)
    }
}