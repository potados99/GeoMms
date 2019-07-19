package com.potados.geomms.core.util

import android.database.Cursor
import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class JsonHelper {
    companion object {
        /**
         * cursor의 내용물을 JSON 배열로 만들어줍니다.
         */
        fun cursorToJson(cursor: Cursor): JsonArray {
            val resultSet = JsonArray()
            if (!cursor.moveToFirst()) return resultSet

            do {
                val rowObject = JsonObject()

                for (i in 0 until cursor.columnCount) {
                    cursor.getColumnName(i)?.let {
                        try {
                            rowObject.addProperty (
                                cursor.getColumnName(i),
                                cursor.getString(i)
                            )
                        } catch (e: Exception) {
                            Log.d("JsonHelper:cursorToJson", e.message)
                        }
                    }
                }

                resultSet.add(rowObject)

            } while (cursor.moveToNext())

            return resultSet
        }
    }
}