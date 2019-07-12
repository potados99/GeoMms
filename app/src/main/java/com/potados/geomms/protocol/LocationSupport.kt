package com.potados.geomms.protocol

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import com.potados.geomms.data.LocationData
import com.potados.geomms.util.Reflection
import com.potados.geomms.util.Types
import java.lang.reflect.Type
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

/**
 * Location 메시지
 */
class LocationSupport {

    companion object {

        /**
         * 메시지 구성 요소:
         * - 위도
         * - 경도
         * - 생성 시각
         *
         * 포맷:
         * [GEOMMS]37.xxxx:127.xxxx:1245135180
         */

        private const val GEO_MMS_PREFIX = "[GEOMMS]"
        private const val FIELD_SPLITTER = ':'

        /**
         * 필드에 관한 정보입니다.
         *
         * 다음 정보를 포함합니다:
         * - 페이로드 안에서의(prefix 제외) 필드의 위치.
         * - 필드의 이름.
         * - 필드 스트링을 숫자로 바꿀 때에 쓸 함수.
         */
        private enum class Field(
            val positionInPayload: Int,         /* 직렬화된 페이로드에서 해당 필드의 위치. */
            val fieldName: String,              /* 해당 필드의 이름. */
            val convert: (String) -> Number     /* 해당 필드를 숫자로 바꾸기 위한 람다식. */
        ) {
            LATITUDE(
                0,
                "latitude",
                {str -> str.toDouble()}
            ),

            LOGITUDE(
                1,
                "longitude",
                {str -> str.toDouble()}
            ),

            DATE(
                2,
                "date",
                {str -> str.toLong()}
            );
        }

        fun parse(body: String): LocationData? {
            /**
             * 예외처리
             */
            if (!isLocationSupportMessage(body)) return null

            /**
             * 페이로드의 필드들 가져오기.
             */
            val payload = body.removePrefix(GEO_MMS_PREFIX)
            val payloadFields = payload.split(FIELD_SPLITTER).also {
                if (it.size != Field.values().size) {
                    Log.d("LocationSupport: parseMessage", "failed to parse message: $body")
                    return null
                }
            }

            /**
             * json으로 만들어서 담을겁니다.
             */
            val json = JsonObject()

            Field.values().forEach {
                val value = try {
                    /**
                     * Convert를 굳이 여기서 해주는 이유는, 숫자 스트링이 무결한지 여기에서 확인하기 위함입니다.
                     * Gson이 검사하게 해도 되는데 그냥 직접 하고 싶었습니다.
                     */
                    it.convert(payloadFields[it.positionInPayload])
                }
                catch (e: Exception) {
                    when (e) {
                        is NumberFormatException -> { /* toDouble이나 toLong에서 문제가 생긴 경우 */
                            Log.d("LocationSupport: parseMessage",
                                "parse error at payload field ${it.positionInPayload}: ${payloadFields[it.positionInPayload]}")
                        }
                        else -> { /* 그렇지 않은 경우 */
                            Log.d("LocationSupport: parseMessage",
                                "unknown error occurred while adding parsed number to json object.")
                        }
                    }

                    return null
                }

                json.addProperty(it.fieldName, value)
            }

            /**
             * LocationData 객체 확보.
             */
            return try {
                Gson().fromJson(json, Types.typeOf<LocationData>())
            }
            catch (e: Exception) {
                when (e) {
                    is JsonSyntaxException -> {
                        Log.d("LocationSupport: parseMessage", "error while parsing json. json syntax incorrect.")
                    }
                    else -> {
                        Log.d("LocationSupport: parseMessage", "unknown error occurred while parsing json.")
                    }
                }
                return null
            }
        }

        /**
         * 메시지가 LocationSupport 메시지인지 확인합니다.
         * 판단 기준은, 앞에 GEO_MMS_PREFIX 접두어가 붙었는가 입니다.
         */
        fun isLocationSupportMessage(body: String): Boolean {
            if (body.isBlank())                     return false   /* 비어있는 메시지 */
            if (!body.startsWith(GEO_MMS_PREFIX))   return false   /* 무관한 메시지 */

            return true
        }

        fun serialize(locationData: LocationData): String? {

            val builder = StringBuilder().append(GEO_MMS_PREFIX)

            Field.values().forEach {
                val value = try {
                    Reflection.readInstanceProperty<Any>(locationData, it.fieldName).toString()
                }
                catch (e: Exception) {
                    Log.d("LocationSupport: serialize", "error occurred while accessing property.")
                    e.printStackTrace()
                    return null
                }

                builder.append(value)
                builder.append(FIELD_SPLITTER)
            }

            builder.trim(FIELD_SPLITTER)

            return builder.toString()
        }
    }

}