package com.potados.geomms.util

import android.content.ContentResolver
import android.net.Uri
import com.google.gson.Gson
import com.potados.geomms.data.ShortMessage
import kotlin.reflect.typeOf

class QueryHelper {
    companion object {
        /**
         *
         */
        inline fun <reified T> queryToCollection(
            resolver: ContentResolver,
            uriString: String,
            projection: Array<String>,
            where: String? = null,
            order: String? = null): T {

            /**
             * 0. Collection만 허용합니다.
             */
            if (T::class.java is Collection<*>) {
                throw IllegalThreadStateException("Wrong generic type: not a collection.")
            }

            /**
             * 1. 쿼리
             */
            val cursor = resolver.query(
                Uri.parse(uriString),
                projection,
                where,
                null,
                order)

            /**
             * 2. json으로 변환
             */
            val parsedJson = JsonHelper.cursorToJson(cursor) /* 커서 알아서 닫아줌. */

            /**
             * 3. json에서 kotlin 개체 collection으로 변환
             */
            return Gson().fromJson(
                parsedJson,
                Types.typeOf<T>()
            )
        }

    }
}