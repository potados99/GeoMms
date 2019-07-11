package com.potados.geomms.util

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import com.google.gson.Gson
import java.lang.RuntimeException

class QueryHelper {
    companion object {
        /**
         * resolver에서 query한 결과를 컬렉션에 집어넣어 반환합니다.
         */
        inline fun <reified T: Collection<*>> queryToCollection(
            resolver: ContentResolver,
            uri: Uri,
            projection: Array<String>?,
            selection: String? = null,
            selectionArgs: Array<String>? = null,
            order: String? = null): T {

            /**
             * 1. 쿼리
             */
            val cursor = resolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                order) ?: throw RuntimeException("Cursor of query result is null.")

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

    /**
     * ContentResolver.query 메소드의 도움말에 따르면,
     * Where 절에 해당하는 string을 "read = 1" 과 같이 selection에 값을 고정하지 말고,
     * "read = ?"와 같이 놓은 뒤 selectionArgs로 해당 값을 넘기는 것이 caching으로 인해
     * 퍼포먼스에 유리하다고 합니다.
     *
     * 이 클래스는 쿼리 조건문을 key와 value 쌍으로 받아서 selection과 selectionArg로 나누어 뽑아줍니다.
     */
    inner class Selection {
        private val selection: StringBuilder = StringBuilder()
        private val selectionArgs: MutableList<String> = mutableListOf()

        fun getSelection(): String? {
            return if (selection.isBlank()) null else selection.toString()
        }

        fun getSelectionArgs(): Array<String>? {
            return if (selectionArgs.isEmpty()) null else selectionArgs.toTypedArray()
        }

        /**
         * 아래의 메소드들은 '?'를 이용한 포맷을 사용하는 유형과, 그렇지 않은 유형으로 나뉩니다.
         * 전자는 비교할 column, 비교 연산자, 비교할 값을 인자로 받습니다.
         * 후자는 비교할 column, 그리고 나머지를 인자로 받습니다.
         */

        /**
         * 조건을 시작합니다. 이전에 있던 조건들은 싹 지웁니다.
         */
        fun <T>of(left: String, operator: String, right: T): Selection {
            return of(Triple(left, operator, right))
        }
        fun <T>of(condition: Triple<String, String, T>): Selection {
            selection.clear()
            selectionArgs.clear()

            addWithPrefix("", condition)

            return this
        }

        /**
         * 포맷을 사용하지 않고 직접 추가합니다.
         * 쿼리 문자열에서 left와 right은 공백 문자로 구분됩니다.
         */
        fun of(left: String, right: String): Selection {
            return of(Pair(left, right))
        }
        fun of(condition: Pair<String, String>): Selection {
            selection.clear()
            selectionArgs.clear()

            addWithPrefixNoFormat("", condition)

            return this
        }

        /**
         * AND 조건을 붙입니다.
         */
        fun <T>and(left: String, operator: String, right: T): Selection {
            return and(Triple(left, operator, right))
        }
        fun <T>and(extraCondition: Triple<String, String, T>): Selection {
            addWithPrefix("AND", extraCondition)
            return this
        }

        /**
         * 포맷을 사용하지 않고 직접 추가합니다.
         * 쿼리 문자열에서 left와 right은 공백 문자로 구분됩니다.
         */
        fun and(left: String, right: String): Selection {
            return and(Pair(left, right))
        }
        fun and(condition: Pair<String, String>): Selection {
            addWithPrefixNoFormat("AND", condition)
            return this
        }

        /**
         * OR 조건을 붙입니다.
         */
        fun <T>or(left: String, operator: String, right: T): Selection {
            return or(Triple(left, operator, right))
        }
        fun <T>or(extraCondition: Triple<String, String, T>): Selection {
            addWithPrefix("OR", extraCondition)
            return this
        }

        /**
         * 포맷을 사용하지 않고 직접 추가합니다.
         * 쿼리 문자열에서 left와 right은 공백 문자로 구분됩니다.
         */
        fun or(left: String, right: String): Selection {
            return or(Pair(left, right))
        }
        fun or(condition: Pair<String, String>): Selection {
            addWithPrefixNoFormat("OR", condition)
            return this
        }

        /**
         * Triple의 첫번째는 key, 두번째는 비교연산자, 세번째는 value입니다.
         */
        private fun <V>addWithPrefix(prefix: String, triple: Triple<String, String, V>) {
            if (prefix.isNotBlank()) {
                selection.append(" $prefix")
            }
            selection.append(" ${triple.first} ${triple.second} ?")

            selectionArgs.add(triple.third.toString())
        }

        /**
         * '?' 포맷 없이 직접 조건을 추가합니다.
         */
        private fun addWithPrefixNoFormat(prefix: String, pair: Pair<String, String>) {
            if (prefix.isNotBlank()) {
                selection.append(" $prefix")
            }
            selection.append(" ${pair.first} ${pair.second}")
        }
    }
}