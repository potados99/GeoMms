package com.potados.geomms.util

import android.content.Context
import androidx.annotation.RawRes

class RawReader(private val context: Context, @RawRes private val resId: Int) {
    fun toStringList(): List<String> = mutableListOf<String>().apply {
        val reader = context.resources.openRawResource(resId).bufferedReader()

        var line: String? = null
        while (let {line = reader.readLine(); line} != null) {
            add(line!!)
        }
    }

}