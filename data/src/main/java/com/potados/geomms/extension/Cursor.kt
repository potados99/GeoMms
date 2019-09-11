
package com.potados.geomms.extension

import android.database.Cursor

fun Cursor.forEach(closeOnComplete: Boolean = true, method: (Cursor) -> Unit = {}) {
    moveToPosition(-1)
    while (moveToNext()) {
        method.invoke(this)
    }

    if (closeOnComplete) {
        close()
    }
}

fun <T> Cursor.map(map: (Cursor) -> T): List<T> {
    return List(count) { position ->
        moveToPosition(position)
        map(this)
    }
}

fun <T> Cursor.mapWhile(map: (Cursor) -> T, predicate: (T) -> Boolean): ArrayList<T> {
    val result = ArrayList<T>()

    moveToPosition(-1)
    while (moveToNext()) {
        val item = map(this)

        if (!predicate(item)) break

        result.add(item)
    }

    return result
}




