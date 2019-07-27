package com.potados.geomms.core.extension

fun List<*>.serialize(): String {
    val builder = StringBuilder()

    this.forEach {
        builder.append("${it.toString()}, ")
    }

    return builder.trim(',', ' ').toString()
}