package com.potados.geomms.feature.location.domain

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.potados.geomms.util.Reflection
import com.potados.geomms.util.Types
import com.potados.geomms.feature.location.data.LSPacket

/**
 * LocationSupport 프로토콜에 관한 static 메소드를 제공합니다.
 * 패킷 parse하기, 직렬화하기, 패킷 만들기 등...
 */
class LSProtocol {

    companion object {

        /**
         * 메시지 구성 요소:
         * - 타입
         * - 연결 식별자
         * - 필드...
         */
        private const val GEO_MMS_PREFIX = "[GEOMMS]"
        private const val FIELD_SPLITTER = ':'

        /**
         * 메시지를 읽어서 LSPacket 객체로 만들어줍니다.
         *
         * @return 접두어가 없거나, 이상하거나, 포맷이 안 맞으면 null을 반환합니다.
         */
        fun parse(body: String): LSPacket? {
            /**
             * 예외처리
             */
            if (!isLocationSupportMessage(body)) {
                Log.w("LSProtocol: parseMessage", "not a LocationSupport packet.")
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
                Log.w("LSProtocol: parseMessage", "necessary fields are missing.")
                return null
            }

            /**
             * 페이로드 중 고정 부분의 첫번째 필드인 type 숫자 값을 가져옵니다.
             */
            val typeNumber = with(LSPacket.Companion.Field.TYPE) {
                convert(payloadFields[positionInPayload])
            }

            /**
             * 해당 typeNumber에 해당하는 PacketType을 가져옵니다.
             */
            val type =
                findType(typeNumber)

            /**
             * 없으면 잘못된 패킷.
             */
            if (type == null) {
                Log.w("LSProtocol: parse", "undefined type: $typeNumber")
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
                            Log.d("LSProtocol: parseMessage",
                                "parse error at payload field ${it.positionInPayload}: ${payloadFields[it.positionInPayload]}")
                        }
                        else -> { /* 그렇지 않은 경우 */
                            Log.d("LSProtocol: parseMessage",
                                "unknown error occurred while adding parsed number to json object.")
                        }
                    }

                    return null
                }

                json.addProperty(it.fieldName, value)
            }

            /**
             * LSPacket 객체 확보.
             */
            return try {
                Gson().fromJson(json, Types.typeOf<LSPacket>())
            }
            catch (e: Exception) {
                when (e) {
                    is JsonSyntaxException -> {
                        Log.d("LSProtocol: parseMessage", "error while parsing json. json syntax incorrect.")
                    }
                    else -> {
                        Log.d("LSProtocol: parseMessage", "unknown error occurred while parsing json.")
                    }
                }
                return null
            }
        }

        /**
         * 메시지가 LSServiceImpl 메시지인지 확인합니다.
         * 판단 기준은, 앞에 GEO_MMS_PREFIX 접두어가 붙었는가 입니다.
         */
        fun isLocationSupportMessage(body: String): Boolean {
            if (body.isBlank())                     return false   /* 비어있는 메시지 */
            if (!body.startsWith(GEO_MMS_PREFIX))   return false   /* 무관한 메시지 */

            return true
        }

        /**
         * LSPacket 객체를 SMS로 보낼 수 있게 직렬화해줍니다.
         * toString()의 기능을 한다고 볼 수 있습니다.
         */
        fun serialize(locationPacket: LSPacket): String? {

            val builder = StringBuilder().append(GEO_MMS_PREFIX)

            val type = LSPacket.Companion.PacketType.values().find { it.number == locationPacket.type }

            if (type == null) {
                Log.d("LSProtocol: serialize", "wrong packet type: ${locationPacket.type}")
                return null
            }

            type.fields.forEach {
                val value = try {
                    Reflection.readInstanceProperty<Any>(locationPacket, it.fieldName).toString()
                }
                catch (e: Exception) {
                    Log.d("LSProtocol: serialize", "error occurred while accessing property.")
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
        fun findType(typeNum: Number): LSPacket.Companion.PacketType? {
            return LSPacket.Companion.PacketType.values().find { it.number == typeNum }
        }
    }
}