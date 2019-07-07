package com.potados.geomms.util

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import com.google.gson.Gson

class QueryHelper {
    companion object {
        /**
         * resolver에서 query한 결과를 컬렉션에 집어넣어 반환합니다.
         */
        inline fun <reified T> queryToCollection(
            resolver: ContentResolver,
            uri: Uri,
            projection: Array<String>,
            where: String? = null,
            order: String? = null): T {

            /**
             * 0. Collection만 허용합니다.
             */
            if (Types.typeOf<T>() is Collection<*>) {
                throw IllegalThreadStateException("Wrong generic type: not a collection.")
            }

            /**
             * 1. 쿼리
             */
            val cursor = resolver.query(
                uri,
                projection,
                where,
                null,
                order)

            /**
             * 2. json으로 변환
             */
            val parsedJson = JsonHelper.cursorToJson(cursor).also { cursor.close() }

            /**
             * 3. json에서 kotlin 개체 collection으로 변환
             */
            return Gson().fromJson(
                parsedJson,
                Types.typeOf<T>()
            )
        }

        fun dumpCursor(cursor: Cursor): List<Map<String, String?>> {
            return mutableListOf<Map<String, String?>>().apply {
                if (!cursor.moveToFirst()) return@apply

                do {
                    val map = HashMap<String, String?>()

                    for (i in 0 until cursor.columnCount) {
                        map[cursor.getColumnName(i)] = cursor.getString(i)
                    }

                    add(map)

                } while (cursor.moveToNext())
            }
        }

    }

}