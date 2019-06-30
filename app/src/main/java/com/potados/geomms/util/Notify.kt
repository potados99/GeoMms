package com.potados.geomms.util

import android.content.Context
import android.widget.Toast

/**
 * Toast의 wrapper입니다.
 * 객체를 만들어서 써도 되고, 그러지 않아도 됩니다.
 */
class Notify(private val context: Context) {

    fun short(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun long(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        fun short(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        fun long(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}